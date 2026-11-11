package com.github.markoc1120.agent.tool.impl;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.markoc1120.agent.Agent;
import com.github.markoc1120.agent.LSPClient;
import com.github.markoc1120.agent.tool.AgentTool;
import com.github.markoc1120.agent.tool.dto.DefinitionParams;
import com.github.markoc1120.agent.tool.dto.DidOpenParams;
import com.github.markoc1120.agent.tool.dto.LSPOperation;
import com.github.markoc1120.agent.tool.dto.Message;
import com.github.markoc1120.agent.tool.dto.Position;
import com.github.markoc1120.agent.tool.dto.ReferenceContext;
import com.github.markoc1120.agent.tool.dto.ReferenceParams;
import com.github.markoc1120.agent.tool.dto.TextDocumentIdentifier;
import com.github.markoc1120.agent.tool.dto.TextDocumentItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

@JsonTypeName("LSP")
@JsonClassDescription("Returns the file, line, and character where a symbol is defined.")
public class LSPTool extends AgentTool {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @JsonProperty("operation")
  public LSPOperation operation;

  @JsonProperty("filePath")
  @JsonPropertyDescription(
      "Absolute path, or a path relative to the agent's current working directory.")
  public String filePath;

  @JsonProperty("line")
  @JsonPropertyDescription("1-based line number of the symbol.")
  @Schema(minimum = "1")
  public int line;

  @JsonProperty("character")
  @JsonPropertyDescription(
      "1-based character offset in UTF-16 code units (same as character count for ASCII).")
  @Schema(minimum = "1")
  public int character;

  @Override
  @SneakyThrows
  public String execute(Agent agent) {
    Path absolutePath = Path.of(filePath).toAbsolutePath();
    String text;
    try {
      text = Files.readString(absolutePath);
    } catch (IOException e) {
      return "LSP: file not found: " + absolutePath;
    }

    LSPClient lspClient = agent.getLspClient();
    String fileName = absolutePath.getFileName().toString();
    String extension = fileName.substring(fileName.lastIndexOf("."));
    String languageId = lspClient.extensionToLanguage.get(extension);
    if (languageId.isEmpty()) {
      return "LSP: unsupported file extension: " + extension;
    }

    String uri = absolutePath.toUri().toString();

    //  notify open (regardless of request type)
    lspClient.send(
        "notify",
        new Message<>(
            "textDocument/didOpen",
            new DidOpenParams(new TextDocumentItem(uri, languageId, 1, text))));

    if (LSPOperation.goToDefinition.equals(operation)) {
      String response =
          lspClient.send(
              "request",
              new Message<>(
                  "textDocument/definition",
                  new DefinitionParams(
                      new TextDocumentIdentifier(uri), new Position(line - 1, character - 1))));
      JsonNode definition = MAPPER.readTree(response);
      return formatDefinition(definition);
    } else if (LSPOperation.findReferences.equals(operation)) {
      String response =
          lspClient.send(
              "request",
              new Message<>(
                  "textDocument/references",
                  new ReferenceParams(
                      new TextDocumentIdentifier(uri),
                      new Position(line - 1, character - 1),
                      new ReferenceContext(true))));
      JsonNode locations = MAPPER.readTree(response);
      return formatReferences(locations);
    }
    return "LSP: operation is not implemented yet";
  }

  private String formatDefinition(JsonNode definition) {
    if (definition == null || definition.isEmpty()) {
      return "LSP: no definition found";
    }

    String parsedUri = parseUri(definition.get("uri"));
    Position position = parsePosition(definition.get("range").get("start"));
    return "Defined in %s:%d:%d".formatted(parsedUri, position.line(), position.character());
  }

  private String formatReferences(JsonNode locations) {
    if (locations == null || !locations.isArray() || locations.isEmpty()) {
      return "LSP: no references found";
    }
    Map<String, List<Position>> byFile =
        locations
            .valueStream()
            .collect(
                Collectors.groupingBy(
                    loc -> parseUri(loc.get("uri")),
                    LinkedHashMap::new,
                    Collectors.mapping(
                        loc -> parsePosition(loc.get("range").get("start")), Collectors.toList())));

    StringBuilder sb = new StringBuilder();
    sb.append("Found %d references across %d files:\n".formatted(locations.size(), byFile.size()));

    byFile.forEach((file, position) -> {
      sb.append("\n").append(file).append(":\n");
      position.forEach(p -> sb.append("  Line %d:%d\n".formatted(p.line(), p.character())));
    });
    return sb.toString().stripTrailing();
  }

  private Position parsePosition(JsonNode position) {
    return new Position(position.get("line").asInt() + 1, position.get("character").asInt() + 1);
  }

  private String parseUri(JsonNode uri) {
    String targetUri = uri.asText();
    return targetUri.substring(targetUri.lastIndexOf("/") + 1);
  }
}
