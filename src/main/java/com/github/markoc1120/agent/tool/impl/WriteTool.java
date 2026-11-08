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

@JsonTypeName("Write")
@JsonClassDescription("Write content to a file")
public class WriteTool extends AgentTool {

  @JsonProperty("file_path")
  @JsonPropertyDescription("The path of the file to write to")
  public String filePath;

  @JsonProperty("content")
  @JsonPropertyDescription("The content to write to the file")
  public String content;

  @Override
  public String execute(Agent agent) {
    Path path = Path.of(filePath);

    try {
      Files.writeString(path, content);
      return "";
    } catch (IOException e) {
      return "";
    }
  }
}
