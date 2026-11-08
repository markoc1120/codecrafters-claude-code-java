package com.github.markoc1120.agent.tool;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

@Getter
public class ToolRegistry {
  private final Map<String, Class<? extends AgentTool>> classByName = new HashMap<>();

  public void add(Class<? extends AgentTool> toolClass) {
    var name = toolClass.getSimpleName();

    final var jsonTypeName = toolClass.getAnnotation(JsonTypeName.class);
    if (jsonTypeName != null) {
      name = jsonTypeName.value();
    }

    classByName.put(name, toolClass);
  }

  public Optional<Class<? extends AgentTool>> find(String name) {
    return Optional.ofNullable(classByName.get(name));
  }
}
