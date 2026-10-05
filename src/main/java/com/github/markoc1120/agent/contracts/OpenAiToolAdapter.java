package com.github.markoc1120.agent.contracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionTool;

import java.util.HashMap;
import java.util.Map;

public final class OpenAiToolAdapter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatCompletionTool adapt(ToolSpec tool) {
        FunctionSpec functionSpec = tool.function();

        JsonNode parametersNode =
                objectMapper.valueToTree(functionSpec.parameters());

        Map<String, JsonValue> parameters = new HashMap<>();

        parametersNode.fields().forEachRemaining(entry -> parameters.put(
                entry.getKey(),
                JsonValue.from(entry.getValue())
        ));

        FunctionParameters functionParameters = FunctionParameters.builder()
                .putAllAdditionalProperties(parameters).build();
        FunctionDefinition function = FunctionDefinition.builder().name(functionSpec.name())
                .description(functionSpec.description()).parameters(functionParameters)
                .build();

        return ChatCompletionTool.builder().function(function).build();
    }
}
