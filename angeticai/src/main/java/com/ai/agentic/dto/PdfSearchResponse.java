package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Result of searching PDF documents by a natural-language prompt")
public record PdfSearchResponse(

    @Schema(description = "AI-generated answer synthesised from the matching document chunks")
    String answer,

    @Schema(description = "List of matching document chunks, ordered by relevance")
    List<PdfDocumentMatch> matches,

    @Schema(description = "The original query that was submitted")
    String query,

    @Schema(description = "Total number of matching chunks returned")
    int matchCount
) {}
