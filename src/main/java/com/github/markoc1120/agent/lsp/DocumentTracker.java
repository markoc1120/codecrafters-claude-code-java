package com.github.markoc1120.agent.lsp;

import com.github.markoc1120.agent.tool.dto.ContentChange;
import com.github.markoc1120.agent.tool.dto.DidChangeParams;
import com.github.markoc1120.agent.tool.dto.DidOpenParams;
import com.github.markoc1120.agent.tool.dto.Message;
import com.github.markoc1120.agent.tool.dto.TextDocumentItem;
import com.github.markoc1120.agent.tool.dto.VersionedTextDocumentIdentifier;
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

  public String uriOf(Path path) {
    return path.toAbsolutePath().toUri().toString();
  }

  public String extensionOf(Path path) {
    String fileName = path.getFileName().toString();
    return fileName.substring(fileName.lastIndexOf("."));
  }

  public String languageIdFor(Path path) {
    return this.lspClient.extensionToLanguage.getOrDefault(this.extensionOf(path), "");
  }

  // TODO: catch sync failures gracefully
  @SneakyThrows
  public void syncAfterSave(Path path, String text) {
    if (languageIdFor(path).isEmpty()) return;
    String uri = this.uriOf(path);

    if (states.containsKey(uri)) {
      int nextVersion = states.get(uri) + 1;
      this.lspClient.send(
          "notify",
          new Message<>(
              "textDocument/didChange",
              new DidChangeParams(
                  new VersionedTextDocumentIdentifier(uri, nextVersion),
                  List.of(new ContentChange(text)))));
      this.states.put(uri, nextVersion);
    } else {
      this.openIfNecessary(path, text);
    }
  }

  @SneakyThrows
  public void openIfNecessary(Path path, String text) {
    String languageId = languageIdFor(path);
    if (languageId.isEmpty()) return;
    String uri = this.uriOf(path);

    if (!states.containsKey(uri)) {
      this.lspClient.send(
          "notify",
          new Message<>(
              "textDocument/didOpen",
              new DidOpenParams(new TextDocumentItem(uri, languageId, 1, text))));
      this.states.put(uri, 1);
    }
  }
}
