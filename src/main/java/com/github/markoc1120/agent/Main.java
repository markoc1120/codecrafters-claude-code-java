package com.github.markoc1120.agent;

import com.github.markoc1120.agent.contracts.OpenAiToolAdapter;
import com.github.markoc1120.agent.contracts.Tool;
import com.github.markoc1120.agent.contracts.ToolSpec;
import com.github.markoc1120.agent.tools.ReadTool;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionTool;

import java.util.List;

public class Main {
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

        String model = System.getenv().getOrDefault(
                "LOCAL_MODEL",
                "anthropic/claude-haiku-4.5"
        );

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        // tool parsing
        List<Tool> tools = List.of(new ReadTool());
        List<ToolSpec> toolSpecs = tools.stream().map(Tool::spec).toList();
        OpenAiToolAdapter adapter = new OpenAiToolAdapter();
        List<ChatCompletionTool> sdkTools = toolSpecs.stream().map(adapter::adapt).toList();

        System.out.println("Using model: " + model);

        ChatCompletion response = client.chat().completions().create(
                ChatCompletionCreateParams.builder()
                        .model(model)
                        .addUserMessage(prompt)
                        .tools(sdkTools)
                        .build()
        );

        if (response.choices().isEmpty()) {
            throw new RuntimeException("no choices in response");
        }

        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.err.println("Logs from your program will appear here!");

        // TODO: Uncomment the line below to pass the first stage
        System.out.print(response.choices().getFirst().message().content().orElse(""));
    }
}
