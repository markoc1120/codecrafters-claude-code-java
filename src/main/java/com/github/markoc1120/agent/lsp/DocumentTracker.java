package com.github.markoc1120.agent.lsp;

import com.github.markoc1120.agent.tool.dto.Message;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;

public class DocumentTracker {
  private final LSPClient lspClient;
  private final Map<String, Integer> states = new HashMap<>();

  public DocumentTracker(LSPClient lspClient) {
    this.lspClient = lspClient;
  }

  // TODO: catch sync failures gracefully
  @SneakyThrows
  public void syncAfterSave(Path path, String text) {
    if (lspClient.languageIdFor(path).isEmpty()) return;
    String uri = LSPClient.uriOf(path);

    if (states.containsKey(uri)) {
      int nextVersion = states.get(uri) + 1;
      lspClient.send(
          "notify",
          new Message<>(
              "textDocument/didChange",
              new DidChangeParams(
                  new VersionedTextDocumentIdentifier(uri, nextVersion),
                  List.of(new ContentChange(text)))));
      states.put(uri, nextVersion);
    } else {
      openIfNecessary(path, text);
    }
  }

  @SneakyThrows
  public void openIfNecessary(Path path, String text) {
    String languageId = lspClient.languageIdFor(path);
    if (languageId.isEmpty()) return;
    String uri = LSPClient.uriOf(path);

    if (!states.containsKey(uri)) {
      lspClient.send(
          "notify",
          new Message<>(
              "textDocument/didOpen",
              new DidOpenParams(new TextDocumentItem(uri, languageId, 1, text))));
      states.put(uri, 1);
    }
  }

  public int getLatestVersion(String uri) {
    return states.getOrDefault(uri, -1);
  }

  private record ContentChange(String text) {}

  private record DidChangeParams(
      VersionedTextDocumentIdentifier textDocument, List<ContentChange> contentChanges) {}

  private record DidOpenParams(TextDocumentItem textDocument) {}

  private record TextDocumentItem(String uri, String languageId, int version, String text) {}

  private record VersionedTextDocumentIdentifier(String uri, int version) {}
}
