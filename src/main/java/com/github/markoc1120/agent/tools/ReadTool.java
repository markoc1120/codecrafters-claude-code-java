package com.github.markoc1120.agent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.markoc1120.agent.contracts.AgentTool;
import com.github.markoc1120.agent.contracts.ToolSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class ReadTool implements AgentTool {

    private static final ToolSpec SPEC = new ToolSpec(
            "Read",
            "Read and return the contents of a file",
            Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "file_path",
                            Map.of("type", "string", "description", "The path to the file to read")
                    ),
                    "required", List.of("file_path")
            )
    );

    @Override
    public ToolSpec spec() {
        return SPEC;
    }

    @Override
    public String execute(JsonNode arguments) {
        Path path = Path.of(arguments.get("file_path").asText());
        String result;
        try {
            result = Files.readString(path);
        } catch (IOException e) {
            return "";
        }
        return result;
    }
}
