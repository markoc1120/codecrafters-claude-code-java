package com.github.markoc1120.agent.tool.dto;

public record ReferenceParams(
    TextDocumentIdentifier textDocument, Position position, ReferenceContext context) {}
