package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "A single row from a spreadsheet that matched the search query")
public record SegmentMatch(

    @Schema(description = "Source file name")
    String fileName,

    @Schema(description = "Row number within the file (1-based)")
    int rowNumber,

    @Schema(description = "Full text of the row as indexed in the vector store")
    String content,

    @Schema(description = "Cosine similarity score (0-1, higher = more relevant)")
    double score,

    @Schema(description = "Structured row data as column header → cell value")
    Map<String, String> rowData
) {}
