package com.github.markoc1120.agent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.markoc1120.agent.contracts.AgentTool;
import com.github.markoc1120.agent.contracts.ToolSpec;

import java.util.List;
import java.util.Map;

public class SkillTool implements AgentTool {
    private static final ToolSpec SPEC = new ToolSpec(
            "Skill",
            "Load a skill's instructions into the conversation",
            Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "name",
                            Map.of("type", "string", "description",
                                    "The name of the skill to use"),
                            "args",
                            Map.of("type", "string", "description",
                                    "Optional arguments for the skill")
                    ),
                    "required", List.of("name")
            )
    );

    @Override
    public ToolSpec spec() {
        return SPEC;
    }

    @Override
    public String execute(JsonNode arguments) {
        return "/%s %s".formatted(arguments.get("name"), arguments.get("args"));
    }
}


