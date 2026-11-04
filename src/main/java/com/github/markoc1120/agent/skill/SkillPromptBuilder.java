package com.github.markoc1120.agent.skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
                
                If a skill matches the user's request, call the Skill tool with its name
                and follow the instructions it returns.
                """.formatted(skillDescriptions);
    }

    private Optional<Skill> findSkill(String name) {
        return skills.stream()
                .filter(s -> s.metadata()
                        .name()
                        .equals(name))
                .findFirst();
    }

    private String replacePlaceholders(String instruction, List<String> args) {
        String result = instruction.replaceAll("\\$ARGUMENTS", String.join(" ", args));

        Pattern pattern = Pattern.compile("\\$(\\d+)");
        return pattern.matcher(result)
                .replaceAll(match -> {
                    int index = Integer.parseInt(match.group(1));
                    String replacement = index < args.size() ? args.get(index) : match.group();
                    return Matcher.quoteReplacement(replacement);

                });
    }

    private String wrapInstruction(Skill s, String instruction) {
        return "Skill: %s (located at %s)\nPaths in the instructions below are relative to that folder.\n\n".formatted(
                s.metadata()
                        .name(), s.location()
                        .getParent()) + instruction;
    }

    public List<String> resolvePrompt(String prompt) {
        String[] parts = prompt.strip()
                .split(" ");

        List<Skill> invoked = new ArrayList<>();
        int i = 0;
        while (i < parts.length && parts[i].startsWith("/")) {
            Optional<Skill> skill = findSkill(parts[i].substring(1));
            if (skill.isEmpty()) {
                break;
            }
            invoked.add(skill.get());
            i++;
        }

        if (invoked.isEmpty()) {
            return List.of(prompt);
        }

        List<String> args = Arrays.asList(parts)
                .subList(i, parts.length);
        return invoked.stream()
                .map(s -> wrapInstruction(s, replacePlaceholders(s.instruction(), args)))
                .toList();
    }
}
