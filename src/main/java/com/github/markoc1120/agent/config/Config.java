package com.github.markoc1120.agent.config;

import org.jetbrains.annotations.Nullable;

public record Config(String apiKey, String baseUrl, String model) {

  public static Config fromEnvironment() {
    return new Config(
        parseEnv("OPENROUTER_API_KEY", null),
        parseEnv("OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1"),
        parseEnv("LOCAL_MODEL", "anthropic/claude-haiku-4.5"));
  }

  private static String parseEnv(String name, @Nullable String defaultValue) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      if (defaultValue == null) {
        throw new IllegalStateException("Undefined environment variable: " + name);
      } else {
        return defaultValue;
      }
    }
    return value;
  }
}
