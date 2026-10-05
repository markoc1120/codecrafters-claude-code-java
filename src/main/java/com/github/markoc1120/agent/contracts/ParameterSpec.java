package com.github.markoc1120.agent.contracts;

import java.util.List;
import java.util.Map;

public record ParameterSpec(
        String type,
        Map<String, PropertySpec> properties,
        List<String> required
) {
}
