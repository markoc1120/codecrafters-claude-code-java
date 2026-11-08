package com.github.markoc1120.agent.tool.impl;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.markoc1120.agent.Agent;
import com.github.markoc1120.agent.tool.AgentTool;
import java.io.IOException;

@JsonTypeName("Bash")
@JsonClassDescription("Execute a shell command")
public class BashTool extends AgentTool {

  @JsonProperty("command")
  @JsonPropertyDescription("The command to execute")
  public String command;

  @Override
  public String execute(Agent agent) {
    ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);

    try {
      Process process = pb.start();
      return new String(process.getInputStream().readAllBytes());
    } catch (IOException e) {
      return e.getMessage();
    }
  }
}
