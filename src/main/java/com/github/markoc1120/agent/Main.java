package com.github.markoc1120.agent;

import com.github.markoc1120.agent.tools.BashTool;
import com.github.markoc1120.agent.tools.ReadTool;
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

        // initialize available tools
        ToolRegistry toolRegistry = new ToolRegistry(List.of(
                new ReadTool(), new WriteTool(), new BashTool()
        ));

        // call the agent
        Agent.create(config, toolRegistry)
                .loop(prompt);
    }
}
