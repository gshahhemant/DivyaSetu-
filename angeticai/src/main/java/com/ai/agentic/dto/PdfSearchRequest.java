package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request to search PDF documents by a natural-language prompt")
public record PdfSearchRequest(

    @Schema(description = "Natural-language query, e.g. 'invoice for March 2024'", required = true)
    String query,

    @Schema(description = "Maximum number of matching document chunks to return (default: 5)")
    Integer maxResults
) {}
