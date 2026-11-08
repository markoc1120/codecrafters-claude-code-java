package com.github.markoc1120.agent.skill;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public record Skill(SkillMetadata metadata, String instruction, Path location) {
  @NotNull
  public String toString() {
    return "- %s: %s".formatted(metadata.name(), metadata.description());
  }
}
