package com.github.markoc1120.agent;

import org.jetbrains.annotations.NotNull;

public record Skill(
        String name,
        String description
) {
    @NotNull
    public String toString() {
        return "- %s: %s".formatted(name, description);
    }
}
