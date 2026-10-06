package com.github.markoc1120.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.markoc1120.agent.contracts.AgentTool;
import com.github.markoc1120.agent.contracts.OpenAiToolAdapter;
import com.github.markoc1120.agent.contracts.ToolSpec;
import com.github.markoc1120.agent.tools.ReadTool;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionTool;

import java.util.List;
import java.util.NoSuchElementException;

public class Main {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    static void main(String[] args) {
        if (args.length < 2 || !"-p".equals(args[0])) {
            System.err.println("Usage: program -p <prompt>");
            System.exit(1);
        }

        String prompt = args[1];

        String apiKey = System.getenv("OPENROUTER_API_KEY");
        String baseUrl = System.getenv("OPENROUTER_BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://openrouter.ai/api/v1";
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OPENROUTER_API_KEY is not set");
        }

        String model = System.getenv()
                .getOrDefault("LOCAL_MODEL", "anthropic/claude-haiku-4.5");

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        // tool parsing
        List<AgentTool> tools = List.of(new ReadTool());
        List<ToolSpec> agentTools = tools.stream()
                .map(AgentTool::spec)
                .toList();
        OpenAiToolAdapter adapter = new OpenAiToolAdapter();
        List<ChatCompletionTool> sdkTools = agentTools.stream()
                .map(adapter::adapt)
                .toList();

        ChatCompletion response = client.chat()
                .completions()
                .create(ChatCompletionCreateParams.builder()
                        .model(model)
                        .addUserMessage(prompt)
                        .tools(sdkTools)
                        .build());

        if (response.choices()
                .isEmpty()) {
            throw new RuntimeException("no choices in response");
        }

        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.err.println("Logs from your program will appear here!");

        // TODO: Uncomment the line below to pass the first stage
        ChatCompletionMessageToolCall.Function toolFunction;
        try {
            toolFunction = response.choices()
                    .getFirst()
                    .message()
                    .toolCalls()
                    .get()
                    .getFirst()
                    .function();
        } catch (NoSuchElementException e) {
            System.out.print(response.choices()
                    .getFirst()
                    .message()
                    .content()
                    .orElse(""));
            return;
        }

        String toolName = toolFunction.name();
        String toolArguments = toolFunction.arguments();

        AgentTool tool = tools.stream()
                .filter(t -> t.spec()
                        .name()
                        .equals(toolName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + toolName));

        String result;
        try {
            result = tool.execute(objectMapper.readTree(toolArguments));
        } catch (JsonProcessingException e) {
            result = "Error: invalid tool arguments: " + e.getMessage();
        }
        System.out.println(result);
    }
}
