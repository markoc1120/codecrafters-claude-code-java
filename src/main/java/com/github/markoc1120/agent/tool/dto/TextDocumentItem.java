package com.github.markoc1120.agent.tool.dto;

public record TextDocumentItem(String uri, String languageId, int version, String text) {}
