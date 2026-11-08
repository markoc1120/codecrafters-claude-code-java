package com.github.markoc1120.agent.tool.impl;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.markoc1120.agent.Agent;
import com.github.markoc1120.agent.tool.AgentTool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@JsonTypeName("Read")
@JsonClassDescription("Read and return the contents of a file")
public class ReadTool extends AgentTool {

  @JsonProperty("file_path")
  @JsonPropertyDescription("The path to the file to read")
  public String filePath;

  @Override
  public String execute(Agent agent) {
    Path path = Path.of(filePath);
    String result;
    try {
      result = Files.readString(path);
    } catch (IOException e) {
      return "";
    }
    return result;
  }
}
