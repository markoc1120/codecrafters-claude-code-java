package com.github.markoc1120.agent;

import com.github.markoc1120.agent.config.Config;
import com.github.markoc1120.agent.skill.SkillParser;
import com.github.markoc1120.agent.skill.SkillRegistry;
import com.github.markoc1120.agent.tool.ToolRegistry;
import com.github.markoc1120.agent.tool.impl.BashTool;
import com.github.markoc1120.agent.tool.impl.ReadTool;
import com.github.markoc1120.agent.tool.impl.SkillTool;
import com.github.markoc1120.agent.tool.impl.WriteTool;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class Main {

  static void main(String[] args) {
    if (args.length < 2 || !"-p".equals(args[0])) {
      System.err.println("Usage: program -p <prompt>");
      System.exit(1);
    }

    // parse prompt
    String prompt = args[1];

    // initialize config (from envs)
    Config config = Config.fromEnvironment();

    // initialize client and reuse it
    OpenAIClient client =
        OpenAIOkHttpClient.builder().apiKey(config.apiKey()).baseUrl(config.baseUrl()).build();

    // initialize available tools
    ToolRegistry toolRegistry = new ToolRegistry();
    toolRegistry.add(ReadTool.class);
    toolRegistry.add(WriteTool.class);
    toolRegistry.add(BashTool.class);
    toolRegistry.add(SkillTool.class);

    // load available skills
    SkillRegistry skillRegistry = new SkillRegistry(new SkillParser());
    skillRegistry.loadGlobalSkills();

    // initialize agent
    Agent agent = new Agent(config, client, toolRegistry, skillRegistry);
    agent.initializeChat(prompt);
    agent.loop().ifPresent(System.out::println);
  }
}
