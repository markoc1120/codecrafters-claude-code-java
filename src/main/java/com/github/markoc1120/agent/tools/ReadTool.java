package com.github.markoc1120.agent.tools;

import com.github.markoc1120.agent.contracts.FunctionSpec;
import com.github.markoc1120.agent.contracts.ParameterSpec;
import com.github.markoc1120.agent.contracts.PropertySpec;
import com.github.markoc1120.agent.contracts.Tool;
import com.github.markoc1120.agent.contracts.ToolSpec;

import java.util.List;
import java.util.Map;

public final class ReadTool implements Tool {
    @Override
    public ToolSpec spec() {
        return new ToolSpec("function",
                new FunctionSpec("Read", "Read and return the contents of a file",
                        new ParameterSpec("object",
                                Map.of("file_path",
                                        new PropertySpec("string", "The path to the file to read")),
                                List.of("file_path"))));
    }

    @Override
    public void execute() {
        // skip for now
    }
}
