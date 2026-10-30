package com.github.markoc1120.agent;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.markoc1120.agent.contracts.AgentTool;
import com.github.markoc1120.agent.skill.Skill;
import com.github.markoc1120.agent.skill.SkillLoader;
import com.github.markoc1120.agent.skill.SkillParser;
import com.github.markoc1120.agent.skill.SkillPromptBuilder;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;

import java.util.ArrayList;
import java.util.List;


public class Agent {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final Config config;
    private final ToolRegistry toolRegistry;
    private final OpenAIClient client;
    private final List<Skill> skills;
    private final SkillPromptBuilder promptBuilder;

    private Agent(
            Config config,
            ToolRegistry toolRegistry,
            OpenAIClient client,
            List<Skill> skills
    ) {
        this.config = config;
        this.toolRegistry = toolRegistry;
        this.client = client;
        this.skills = skills;
        this.promptBuilder = new SkillPromptBuilder();
    }

    public static Agent create(Config config, ToolRegistry toolRegistry) {
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(config.apiKey())
                .baseUrl(config.baseUrl())
                .build();

        List<Skill> skills = new SkillLoader(
                new SkillParser()
        ).loadGlobalSkills();

        return new Agent(config, toolRegistry, client, skills);
    }

    private ChatCompletion callModel(List<ChatCompletionMessageParam> messages) {
        return client.chat()
                .completions()
                .create(ChatCompletionCreateParams.builder()
                        .model(config.model())
                        .messages(messages)
                        .tools(toolRegistry.sdkTools())
                        .build());
    }

    public void loop(String prompt) {
        List<ChatCompletionMessageParam> messages = new ArrayList<>();

        // For now, it is OpenAI specific types (library agnostic solution needs own types + adapter)
        String systemPrompt = promptBuilder.buildSystemPrompt(skills);
        String userPrompt = promptBuilder.resolvePrompt(prompt, skills);

        messages.add(ChatCompletionMessageParam.ofSystem(
                ChatCompletionSystemMessageParam.builder()
                        .content(systemPrompt)
                        .build()
        ));

        // initial user prompt -> as agent calls tools we append the response, and everytime we call the agent
        // we give the whole conversation (stateless)
        messages.add(ChatCompletionMessageParam.ofUser(
                ChatCompletionUserMessageParam.builder()
                        .content(userPrompt)
                        .build()
        ));

        while (true) {
            // calling agent with the whole conversation
            ChatCompletion response = callModel(messages);

            if (response.choices()
                    .isEmpty()) {
                throw new RuntimeException("no choices in response");
            }
            System.err.println("Logs from your program will appear here!");

            ChatCompletionMessage msg = response.choices()
                    .getFirst()
                    .message();

            // retrieve all the toolCalls
            List<ChatCompletionMessageToolCall> toolCalls = msg.toolCalls()
                    .orElse(List.of());
            if (toolCalls.isEmpty()) {
                System.out.print(msg.content()
                        .orElse(""));
                return;
            }
            // add the response (type: assistant) to the conversation (aka messages)
            messages.add(ChatCompletionMessageParam.ofAssistant(msg.toParam()));

            // execute each tool and add their responses (type: tool) to the conversation
            for (ChatCompletionMessageToolCall toolCall : toolCalls) {
                ChatCompletionMessageToolCall.Function toolCallFunction = toolCall.function();
                String toolName = toolCallFunction.name();
                String toolArguments = toolCallFunction.arguments();

                AgentTool tool = toolRegistry.findTool(toolName);
                String result;
                try {
                    result = tool.execute(objectMapper.readTree(toolArguments));
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("Invalid tool arguments for: " + toolName,
                            e);
                }
                messages.add(ChatCompletionMessageParam.ofTool(
                        ChatCompletionToolMessageParam.builder()
                                .toolCallId(toolCall.id())
                                .content(result)
                                .build()));
            }
        }
    }
}
