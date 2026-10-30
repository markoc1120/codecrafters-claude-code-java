package com.github.markoc1120.agent.skill;

import java.util.List;
import java.util.stream.Collectors;

public class SkillPromptBuilder {
    public String buildSystemPrompt(List<Skill> skills) {
        if (skills.isEmpty()) {
            return "No skills available.";
        }

        String skillDescriptions = skills.stream()
                .map(skill -> "- %s: %s".formatted(
                        skill.metadata()
                                .name(),
                        skill.metadata()
                                .description()
                ))
                .collect(Collectors.joining("\n"));

        return """
                You have access to the following skills:
                
                %s
                """.formatted(skillDescriptions);
    }

    public String resolvePrompt(String prompt, List<Skill> skills) {
        return skills.stream()
                .filter(skill -> skill.getPrompt()
                        .equals(prompt))
                .map(Skill::instruction)
                .findFirst()
                .orElse(prompt);
    }
}
