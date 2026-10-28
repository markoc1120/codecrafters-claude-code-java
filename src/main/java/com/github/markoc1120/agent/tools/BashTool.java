package com.github.markoc1120.agent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.markoc1120.agent.contracts.AgentTool;
import com.github.markoc1120.agent.contracts.ToolSpec;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BashTool implements AgentTool {
    private static final ToolSpec SPEC = new ToolSpec(
            "Bash",
            "Execute a shell command",
            Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "command",
                            Map.of("type", "string", "description",
                                    "The command to execute")
                    ),
                    "required", List.of("command")
            )
    );

    @Override
    public ToolSpec spec() {
        return SPEC;
    }

    @Override
    public String execute(JsonNode arguments) {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", arguments.get("command")
                .asText());

        try {
            Process process = pb.start();
            return new String(process.getInputStream()
                    .readAllBytes());
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}


