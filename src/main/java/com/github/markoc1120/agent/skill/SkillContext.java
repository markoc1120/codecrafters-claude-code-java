package com.github.markoc1120.agent.skill;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SkillContext {
  DEFAULT,
  FORK;

  @JsonCreator
  public static SkillContext fromString(String value) {
    return SkillContext.valueOf(value.toUpperCase());
  }
}
