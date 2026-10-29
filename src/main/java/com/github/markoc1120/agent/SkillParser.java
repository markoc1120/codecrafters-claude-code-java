package com.github.markoc1120.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;

public class SkillParser {
    private static final ObjectMapper ObjectMapper = new ObjectMapper(new YAMLFactory());

    public Skill parse(Path path) throws IOException {
        return ObjectMapper.readValue(path.toFile(), Skill.class);
    }
}
