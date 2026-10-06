package com.github.markoc1120.agent.contracts;

import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionTool;


public final class OpenAiToolAdapter implements ToolAdapter<ChatCompletionTool> {
    @Override
    public ChatCompletionTool adapt(ToolSpec toolSpec) {
        FunctionParameters.Builder paramsBuilder = FunctionParameters.builder();
        toolSpec.parameters()
                .forEach((key, value) -> paramsBuilder.putAdditionalProperty(key,
                        JsonValue.from(value)));

        FunctionDefinition function = FunctionDefinition.builder()
                .name(toolSpec.name())
                .description(toolSpec.description())
                .parameters(paramsBuilder.build())
                .build();

        return ChatCompletionTool.builder()
                .function(function)
                .build();
    }
}
