package com.github.markoc1120.agent.lsp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.markoc1120.agent.tool.dto.Position;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;

public class DiagnosticsTracker {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final LSPClient lspClient;
  private final Map<String, Set<IssueKey>> lastShown = new HashMap<>();
  private int cursor = 0;

  public DiagnosticsTracker(LSPClient lspClient) {
    this.lspClient = lspClient;
  }

  @SneakyThrows
  private List<NotificationItem> fetchNotifications() {
    String body = lspClient.send("notifications", new NotificationsRequest(cursor, 0));
    NotificationsResponse response = MAPPER.readValue(body, NotificationsResponse.class);
    cursor = response.cursor();
    return response.notifications();
  }

  @SneakyThrows
  public Optional<String> collectDiagnostics() {
    DocumentTracker documentTracker = lspClient.getDocumentTracker();
    StringBuilder blocks = new StringBuilder();

    for (NotificationItem item : fetchNotifications()) {
      if (!"textDocument/publishDiagnostics".equals(item.method())) continue;

      PublishDiagnosticsParams report =
          MAPPER.treeToValue(item.params(), PublishDiagnosticsParams.class);

      String uri = report.uri();
      if (report.version() != null && documentTracker.getLatestVersion(uri) != report.version()) {
        continue;
      }

      Set<IssueKey> shown = lastShown.getOrDefault(uri, Set.of());
      Set<IssueKey> current = new HashSet<>();
      StringBuilder fileBlock = new StringBuilder();
      for (Diagnostic diagnostic : report.diagnostics()) {
        IssueKey issue =
            new IssueKey(
                diagnostic.range().start().line() + 1,
                diagnostic.range().start().character() + 1,
                diagnostic.message());
        current.add(issue);
        if (shown.contains(issue)) continue;
        fileBlock
            .append("  [Line ")
            .append(issue.line())
            .append(":")
            .append(issue.character())
            .append("] ")
            .append(issue.message())
            .append("\n");
      }

      lastShown.put(uri, current);
      if (!fileBlock.isEmpty()) {
        blocks
            .append("\n")
            .append(LSPClient.fileNameOf(Path.of(URI.create(uri))))
            .append(":\n")
            .append(fileBlock);
      }
    }

    if (blocks.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        "<new-diagnostics>The following new diagnostic issues were detected:\n"
            + blocks
            + "</new-diagnostics>");
  }

  private record NotificationsRequest(int after, int timeoutMs) {}

  private record Range(Position start, Position end) {}

  private record Diagnostic(
      Range range, int severity, String message, String source, String code) {}

  private record PublishDiagnosticsParams(
      String uri, Integer version, List<Diagnostic> diagnostics) {}

  private record IssueKey(int line, int character, String message) {}

  private record NotificationItem(String method, JsonNode params) {}

  private record NotificationsResponse(int cursor, List<NotificationItem> notifications) {}
}
