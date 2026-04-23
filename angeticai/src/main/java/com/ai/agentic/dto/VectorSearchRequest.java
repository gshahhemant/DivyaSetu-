package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Query to run against the vector store")
public record VectorSearchRequest(

    @Schema(
        description = "Natural-language question or search text",
        example = "Show all pending orders above 5000"
    )
    String query,

    @Schema(
        description = "Maximum number of matching rows to retrieve (default: 10)",
        example = "10"
    )
    Integer maxResults,

    @Schema(
        description = "Optional: restrict search to specific files (e.g. [\"sales.xlsx\"]). " +
                      "Leave empty to search across all indexed files.",
        example = "[\"sales.xlsx\", \"orders.csv\"]"
    )
    List<String> fileNames
) {
    public int effectiveMaxResults() {
        return (maxResults == null || maxResults <= 0) ? 10 : maxResults;
    }
}
