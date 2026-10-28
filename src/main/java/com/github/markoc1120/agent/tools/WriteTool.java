package com.github.markoc1120.agent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.markoc1120.agent.contracts.AgentTool;
import com.github.markoc1120.agent.contracts.ToolSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class WriteTool implements AgentTool {
    private static final ToolSpec SPEC = new ToolSpec(
            "Write",
            "Write content to a file",
            Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "file_path",
                            Map.of("type", "string", "description",
                                    "The path of the file to write to"),
                            "content",
                            Map.of("type", "string", "description",
                                    "The content to write to the file")
                    ),
                    "required", List.of("file_path", "content")
            )
    );

    @Override
    public ToolSpec spec() {
        return SPEC;
    }

    @Override
    public String execute(JsonNode arguments) {
        Path path = Path.of(arguments.get("file_path")
                .asText());
//        String content = String.valueOf(arguments.get("content").asText());

        try {
            Files.writeString(path, arguments.get("content")
                    .asText());
            return "";
        } catch (IOException e) {
            return "";
        }
    }
}


