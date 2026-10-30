package com.github.markoc1120.agent.skill;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SkillLoader {
    private final SkillParser parser;

    public SkillLoader(SkillParser parser) {
        this.parser = parser;
    }

    public List<Skill> loadGlobalSkills() {
        Path skillsDirectory = Path.of(".claude", "skills");

        if (!Files.isDirectory(skillsDirectory)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(skillsDirectory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName()
                            .toString()
                            .equals("SKILL.md"))
                    .map(this::loadSkill)
                    .flatMap(Optional::stream)
                    .toList();

        } catch (IOException e) {
            System.err.println("Failed to discover skills: " + e);
            return List.of();
        }
    }

    private Optional<Skill> loadSkill(Path path) {
        try {
            return Optional.of(parser.parse(path));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println(
                    "Failed to parse skill: " + path + ": " + e.getMessage()
            );
            return Optional.empty();
        }
    }
}
