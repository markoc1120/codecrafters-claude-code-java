package com.github.markoc1120.agent.skill;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SkillPromptBuilder {
    private final List<Skill> skills;

    public SkillPromptBuilder(List<Skill> skills) {
        this.skills = skills;
    }

    public String buildSystemPrompt() {
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

    private String replacePlaceholders(String instruction, List<String> values) {
        String result = instruction.replaceAll("\\$ARGUMENTS", String.join(" ", values));

        Pattern pattern = Pattern.compile("\\$(\\d+)");
        return pattern.matcher(result)
                .replaceAll(match -> {
                    int index = Integer.parseInt(match.group(1));

                    return index < values.size() ? Matcher.quoteReplacement(
                            values.get(index)) : match.group();

                });
    }

    private String resolveSkill(String prompt) {
        String[] parts = prompt.split(" ");
        String skill = parts[0];
        List<String> arguments = Arrays.asList(parts)
                .subList(1, parts.length);

        String instruction = skills.stream()
                .filter(s -> s.metadata()
                        .name()
                        .equals(skill))
                .map(Skill::instruction)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown skill: " + skill));
        return replacePlaceholders(instruction, arguments);
    }

    public List<String> resolvePrompt(String prompt) {
        if (!prompt.startsWith("/")) {
            return List.of(prompt);
        }

        String[] parts = prompt.split("/");
        return Arrays.stream(parts)
                .skip(1)
                .map(String::strip)
                .map(this::resolveSkill)
                .toList();
    }
}
