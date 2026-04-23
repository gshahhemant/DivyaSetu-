package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Search results from one spreadsheet file")
public record FileSearchResult(

    @Schema(description = "File name (e.g. sales.xlsx)")
    String fileName,

    @Schema(description = "AI-generated summary of what was found in this file")
    String summary,

    @Schema(description = "Rows that matched the prompt (column header → cell value)")
    List<Map<String, String>> matchedRows,

    @Schema(description = "Total rows scanned in this file")
    int totalRowsScanned,

    @Schema(description = "Number of matching rows found")
    int matchedCount
) {}
