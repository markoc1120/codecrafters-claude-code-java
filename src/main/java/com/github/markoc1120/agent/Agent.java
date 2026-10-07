package com.github.markoc1120.agent;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.markoc1120.agent.contracts.AgentTool;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import org.jetbrains.annotations.Nullable;


public class Agent {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final Config config;
    private final ToolRegistry toolRegistry;
    private final OpenAIClient client;


    private Agent(Config config, ToolRegistry toolRegistry) {
        this.config = config;
        this.toolRegistry = toolRegistry;
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(config.apiKey())
                .baseUrl(config.baseUrl())
                .build();
    }

    public static Agent create(Config config, ToolRegistry toolRegistry) {
        return new Agent(config, toolRegistry);
    }

    private ChatCompletion callModel(String prompt) {
        return client.chat()
                .completions()
                .create(ChatCompletionCreateParams.builder()
                        .model(config.model())
                        .addUserMessage(prompt)
                        .tools(toolRegistry.sdkTools())
                        .build());
    }

    private @Nullable ChatCompletionMessageToolCall.Function getToolFunction(
            ChatCompletionMessage msg) {
        return msg.toolCalls()
                .flatMap(toolCalls -> toolCalls.stream()
                        .findFirst())
                .map(ChatCompletionMessageToolCall::function)
                .orElse(null);
    }

    public void loop(String prompt) {
        ChatCompletion response = callModel(prompt);
        if (response.choices()
                .isEmpty()) {
            throw new RuntimeException("no choices in response");
        }
        System.err.println("Logs from your program will appear here!");

        ChatCompletionMessage msg = response.choices()
                .getFirst()
                .message();

        ChatCompletionMessageToolCall.Function toolFunction = getToolFunction(msg);
        if (toolFunction == null) {
            System.out.print(msg.content().orElse(""));
            return;
        }

        String toolName = toolFunction.name();
        String toolArguments = toolFunction.arguments();

        AgentTool tool = toolRegistry.findTool(toolName);

        String result;
        try {
            result = tool.execute(objectMapper.readTree(toolArguments));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid tool arguments for: " + toolName, e);
        }
        System.out.println(result);
    }
}
