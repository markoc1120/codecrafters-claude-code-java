package com.github.markoc1120.agent.contracts;

public interface ToolAdapter<T> {
    T adapt(ToolSpec toolSpec);
}
