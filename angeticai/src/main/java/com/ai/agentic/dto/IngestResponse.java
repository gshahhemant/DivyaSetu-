package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Result of ingesting spreadsheet files into the vector store")
public record IngestResponse(

    @Schema(description = "Human-readable summary of the ingestion")
    String message,

    @Schema(description = "Number of files processed")
    int filesIngested,

    @Schema(description = "Total rows embedded and stored across all files")
    int totalSegmentsStored,

    @Schema(description = "Per-file breakdown")
    List<FileIngestDetail> fileDetails
) {}
