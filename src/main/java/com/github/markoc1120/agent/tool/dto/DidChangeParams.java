package com.github.markoc1120.agent.tool.dto;

import java.util.List;

public record DidChangeParams(
    VersionedTextDocumentIdentifier textDocument, List<ContentChange> contentChanges) {}
