package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request payload to search text across local spreadsheet files")
public record SpreadsheetSearchRequest(

    @Schema(
        description = "Natural-language text to search for across the spreadsheet files",
        example = "Find all orders with status pending"
    )
    String prompt,

    @Schema(
        description = "Optional list of specific file names to search (e.g. [\"sales.xlsx\", \"orders.csv\"]). " +
                      "Leave empty to search ALL files in the configured folder.",
        example = "[\"sales.xlsx\", \"hr.csv\"]"
    )
    List<String> fileNames,

    @Schema(
        description = "Maximum number of matching rows to return per file (default: 10)",
        example = "10"
    )
    Integer maxResultsPerFile
) {
    public int effectiveMaxResults() {
        return (maxResultsPerFile == null || maxResultsPerFile <= 0) ? 10 : maxResultsPerFile;
    }
}
