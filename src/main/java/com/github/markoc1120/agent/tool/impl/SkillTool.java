package com.github.markoc1120.agent.tool.impl;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.markoc1120.agent.Agent;
import com.github.markoc1120.agent.skill.SkillRegistry;
import com.github.markoc1120.agent.tool.AgentTool;
import java.util.Optional;

@JsonTypeName("Skill")
@JsonClassDescription("Load a skill's instructions into the conversation")
public class SkillTool extends AgentTool {

  @JsonProperty("name")
  @JsonPropertyDescription("The name of the skill to use")
  public String name;

  @JsonProperty("args")
  @JsonPropertyDescription("Optional arguments for the skill")
  public Optional<String> argumentsString;

  @Override
  public String execute(Agent agent) {
    SkillRegistry skillRegistry = agent.getSkillRegistry();
    String skillArgs = argumentsString.orElse("");
    return String.join("\n", skillRegistry.resolvePrompt("/%s %s".formatted(name, skillArgs)));
  }
}
