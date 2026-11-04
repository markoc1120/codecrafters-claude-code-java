package com.github.markoc1120.agent;

import com.github.markoc1120.agent.skill.Skill;
import com.github.markoc1120.agent.skill.SkillLoader;
import com.github.markoc1120.agent.skill.SkillParser;
import com.github.markoc1120.agent.skill.SkillPromptBuilder;
import com.github.markoc1120.agent.tools.BashTool;
import com.github.markoc1120.agent.tools.ReadTool;
import com.github.markoc1120.agent.tools.SkillTool;
import com.github.markoc1120.agent.tools.WriteTool;

import java.util.List;

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

        // load available skills
        List<Skill> skills = new SkillLoader(
                new SkillParser()
        ).loadGlobalSkills();

        // create promptBuilder
        SkillPromptBuilder promptBuilder = new SkillPromptBuilder(skills);

        // initialize available tools
        ToolRegistry toolRegistry = new ToolRegistry(List.of(
                new ReadTool(), new WriteTool(), new BashTool(), new SkillTool(promptBuilder)
        ));

        // call the agent
        Agent.create(config, toolRegistry, promptBuilder)
                .loop(prompt);
    }
}
