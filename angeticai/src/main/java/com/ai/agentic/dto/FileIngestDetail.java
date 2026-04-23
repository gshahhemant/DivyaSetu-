package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ingestion result for a single spreadsheet file")
public record FileIngestDetail(

    @Schema(description = "File name")
    String fileName,

    @Schema(description = "Number of rows successfully embedded and stored")
    int rowsIngested,

    @Schema(description = "OK, or an error message if ingestion failed")
    String status
) {}
