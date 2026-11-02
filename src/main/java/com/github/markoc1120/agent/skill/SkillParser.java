package com.github.markoc1120.agent.skill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SkillParser {
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public Skill parse(Path path) throws IOException {
        String content = Files.readString(path);

        String[] parts = content.split("(?m)^---\\s*$", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid YAML frontmatter: " + path);
        }
        SkillMetadata metadata = yamlMapper.readValue(parts[1], SkillMetadata.class);
        String instruction = parts[2].strip();
        return new Skill(metadata, instruction, path);
    }
}
