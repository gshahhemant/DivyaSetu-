package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Aggregated search results across all searched spreadsheet files")
public record SpreadsheetSearchResponse(

    @Schema(description = "AI-generated overall summary combining findings from all files")
    String overallSummary,

    @Schema(description = "Per-file results")
    List<FileSearchResult> fileResults,

    @Schema(description = "Total files searched")
    int filesSearched,

    @Schema(description = "Number of files that had at least one matching row")
    int filesWithMatches,

    @Schema(description = "Total matched rows across all files")
    int totalMatchedRows
) {}
