package com.github.markoc1120.agent.skill;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private String replacePlaceholders(String instruction, List<String> values) {
        instruction = instruction.replaceAll("\\$ARGUMENTS", String.join(" ", values));
        Pattern pattern = Pattern.compile("\\$(\\d+)");
        Matcher matcher = pattern.matcher(instruction);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));

            if (index >= 0 && index < values.size()) {
                matcher.appendReplacement(
                        result,
                        Matcher.quoteReplacement(values.get(index))
                );
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public String resolvePrompt(String prompt, List<Skill> skills) {
        if (!prompt.startsWith("/")) {
            return prompt;
        }
        String[] parts = prompt.split(" ");
        String skill = parts[0];
        List<String> arguments = Arrays.asList(parts)
                .subList(1, parts.length);

        String instruction = skills.stream()
                .filter(s -> s.getPrompt()
                        .startsWith(skill))
                .map(Skill::instruction)
                .findFirst()
                .orElse("");

        if (arguments.isEmpty()) {
            return instruction;
        }

        return replacePlaceholders(instruction, arguments);
    }
}
