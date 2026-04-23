package com.ai.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single PDF document chunk that matched the search query")
public record PdfDocumentMatch(

    @Schema(description = "Name of the PDF file, e.g. invoice-2024.pdf")
    String fileName,

    @Schema(description = "Full path to the PDF file")
    String filePath,

    @Schema(description = "Page number within the PDF where this text was found (1-based)")
    int pageNumber,

    @Schema(description = "Relevant text snippet extracted from the page")
    String textSnippet,

    @Schema(description = "Semantic similarity score (0.0 – 1.0); higher is more relevant")
    double score
) {}
