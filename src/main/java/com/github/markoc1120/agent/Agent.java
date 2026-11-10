package com.github.markoc1120.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.markoc1120.agent.config.Config;
import com.github.markoc1120.agent.skill.SkillRegistry;
import com.github.markoc1120.agent.tool.AgentTool;
import com.github.markoc1120.agent.tool.ToolRegistry;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletion.Choice.FinishReason;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

@Getter
public class Agent {
  private final Config config;
  private final OpenAIClient client;
  private final ChatCompletionCreateParams.Builder history;

  private final ToolRegistry toolRegistry;
  private final SkillRegistry skillRegistry;
  private final LSPClient lspClient;
  private final ObjectMapper mapper = new ObjectMapper();

  public Agent(
      Config config,
      OpenAIClient client,
      ToolRegistry toolRegistry,
      SkillRegistry skillRegistry,
      LSPClient lspClient) {
    this.config = config;
    this.client = client;
    this.history = ChatCompletionCreateParams.builder().model(config.model());

    this.toolRegistry = toolRegistry;
    this.skillRegistry = skillRegistry;
    this.lspClient = lspClient;

    for (Class<? extends AgentTool> toolClass : toolRegistry.getClassByName().values()) {
      this.history.addTool(toolClass);
    }
  }

  public Agent fork() {
    return new Agent(config, client, toolRegistry, skillRegistry, lspClient);
  }

  public void initializeChat(String prompt) {
    history.addSystemMessage(skillRegistry.buildSystemPrompt());

    // initial user prompts -> as agent calls tools we append the response, and everytime we call
    // the agent
    // we give the whole conversation (stateless)
    skillRegistry.resolvePrompt(prompt).forEach(this::pushMessage);
  }

  public void pushMessage(String message) {
    history.addUserMessage(message);
  }

  public Optional<String> loop() {
    while (true) {
      // calling agent with the whole conversation
      List<ChatCompletion.Choice> choices =
          client.chat().completions().create(history.build()).choices();
      if (choices.isEmpty()) {
        throw new RuntimeException("no choices in response");
      }

      ChatCompletion.Choice choice = choices.getFirst();
      ChatCompletionMessage message = choice.message();
      history.addMessage(message);

      if (FinishReason.TOOL_CALLS.equals(choice.finishReason())) {
        Optional<List<ChatCompletionMessageToolCall>> toolCalls = message.toolCalls();
        if (toolCalls.isEmpty()) {
          throw new IllegalStateException("No toolCall is found in the message");
        }

        for (ChatCompletionMessageToolCall toolCall : toolCalls.get()) {

          ChatCompletionMessageFunctionToolCall toolFunction = toolCall.asFunction();
          String result = callFunction(toolFunction.function());

          history.addMessage(
              ChatCompletionToolMessageParam.builder()
                  .toolCallId(toolFunction.id())
                  .content(result)
                  .build());
        }

      } else if (FinishReason.STOP.equals(choice.finishReason())) {
        return message.content();
      } else {
        System.err.println("Chat finished with: " + choice.finishReason());
        return Optional.empty();
      }
    }
  }

  private String callFunction(ChatCompletionMessageFunctionToolCall.Function function) {
    Class<? extends AgentTool> toolClass =
        toolRegistry
            .find(function.name())
            .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + function.name()));
    return function.arguments(toolClass).execute(this);
  }
}
