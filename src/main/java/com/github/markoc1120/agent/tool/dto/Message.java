package com.github.markoc1120.agent.tool.dto;

public record Message<P>(String method, P params) {}
