package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for AI chat")
public record ChatRequest(

    @Schema(
        description = "Your natural-language question about the database data",
        example = "How many contacts are in New York?"
    )
    String prompt
) {
}
