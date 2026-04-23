package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response from a vector (semantic) search across indexed spreadsheet rows")
public record VectorSearchResponse(

    @Schema(description = "AI-generated natural-language answer synthesised from the retrieved rows")
    String answer,

    @Schema(description = "Individual matched rows with similarity scores")
    List<SegmentMatch> segments,

    @Schema(description = "The original query")
    String query,

    @Schema(description = "Number of segments returned")
    int totalFound
) {}
