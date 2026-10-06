package com.github.markoc1120.agent.contracts;

import java.util.Map;

public record ToolSpec(
        String name,
        String description,
        Map<String, Object> parameters
) {
}