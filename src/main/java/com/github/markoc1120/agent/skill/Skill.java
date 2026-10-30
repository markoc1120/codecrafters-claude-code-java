package com.github.markoc1120.agent.skill;

import org.jetbrains.annotations.NotNull;

public record Skill(
        SkillMetadata metadata,
        String instruction
) {
    @NotNull
    public String toString() {
        return "- %s: %s".formatted(metadata.name(), metadata.description());
    }

    public String getPrompt() {
        return "/" + metadata.name();
    }
}
