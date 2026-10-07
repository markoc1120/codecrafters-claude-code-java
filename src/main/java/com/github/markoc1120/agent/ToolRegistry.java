package com.github.markoc1120.agent;

import com.github.markoc1120.agent.contracts.AgentTool;
import com.github.markoc1120.agent.contracts.OpenAiToolAdapter;
import com.openai.models.chat.completions.ChatCompletionTool;

import java.util.List;

public record ToolRegistry(List<AgentTool> tools) {
    private static final OpenAiToolAdapter ADAPTER = new OpenAiToolAdapter();

    public ToolRegistry {
        tools = List.copyOf(tools);
    }

    public List<ChatCompletionTool> sdkTools() {
        return tools.stream()
                .map(AgentTool::spec)
                .map(ADAPTER::adapt)
                .toList();
    }

    public AgentTool findTool(String toolName) {
        return tools.stream()
                .filter(t -> t.spec()
                        .name()
                        .equals(toolName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + toolName));
    }
}
