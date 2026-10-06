package com.github.markoc1120.agent.contracts;

import com.fasterxml.jackson.databind.JsonNode;

public interface AgentTool {
    ToolSpec spec();

    String execute(JsonNode arguments);
}
