package com.ai.agentic.controller;

import com.ai.agentic.dto.FileSearchResult;
import com.ai.agentic.dto.SpreadsheetSearchRequest;
import com.ai.agentic.dto.SpreadsheetSearchResponse;
import com.ai.agentic.service.LocalSpreadsheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/local-sheets")
@Tag(
    name = "Local Spreadsheet Search",
    description = """
        Reads .xlsx and .csv files from the local C:/google_sheets folder and uses
        AI (Ollama) to find rows matching a natural-language prompt.
        Supports searching a single file or all files at once.
        """
)
public class LocalSpreadsheetController {

    private final LocalSpreadsheetService spreadsheetService;

    public LocalSpreadsheetController(LocalSpreadsheetService spreadsheetService) {
        this.spreadsheetService = spreadsheetService;
    }

    // ─────────────────── GET /files  — list available files ──────────────────

    @GetMapping("/files")
    @Operation(
        summary = "List all spreadsheet files",
        description = "Returns the names of all .xlsx and .csv files found in C:/google_sheets."
    )
    public ResponseEntity<List<String>> listFiles() {
        try {
            return ResponseEntity.ok(spreadsheetService.listFiles());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─────────────────── GET /files/{fileName}/rows  — raw rows ──────────────

    @GetMapping("/files/{fileName}/rows")
    @Operation(
        summary = "Read all rows from a single file",
        description = "Returns every row from the specified .xlsx or .csv file as column-header → cell-value maps. Useful for previewing file content."
    )
    public ResponseEntity<List<Map<String, String>>> readRows(
            @Parameter(description = "File name in C:/google_sheets, e.g. sales.xlsx")
            @PathVariable String fileName) {
        try {
            return ResponseEntity.ok(spreadsheetService.readAllRows(fileName));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─────────────────── GET /search  — quick single-file search via GET ─────

    @GetMapping("/search")
    @Operation(
        summary = "Search a single file by prompt (GET)",
        description = "Quick GET search. Pass ?prompt= and ?fileName= (optional — omit to search ALL files)."
    )
    public ResponseEntity<SpreadsheetSearchResponse> searchGet(
            @Parameter(description = "Natural-language search text", required = true)
            @RequestParam String prompt,

            @Parameter(description = "File name to search, e.g. sales.xlsx (optional — searches all files if omitted)")
            @RequestParam(required = false) String fileName,

            @Parameter(description = "Max matching rows per file (default: 10)")
            @RequestParam(required = false, defaultValue = "10") int maxResults) {

        if (prompt.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        List<String> targets = (fileName != null && !fileName.isBlank())
                ? List.of(fileName)
                : Collections.emptyList();
        try {
            return ResponseEntity.ok(spreadsheetService.search(prompt, targets, maxResults));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse("Folder error: " + e.getMessage()));
        }
    }

    // ─────────────────── POST /search  — multi-file search via POST ──────────

    @PostMapping("/search")
    @Operation(
        summary = "Search across multiple files by prompt (POST)",
        description = """
            Send a natural-language prompt to search one or many spreadsheet files.
            The AI reads every row in each file and returns the rows that best match your prompt,
            along with an overall AI-generated summary.

            - Supply `fileNames` to target specific files.
            - Leave `fileNames` empty to search ALL files in C:/google_sheets.
            """
    )
    public ResponseEntity<SpreadsheetSearchResponse> search(@RequestBody SpreadsheetSearchRequest request) {
        if (request.prompt() == null || request.prompt().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            SpreadsheetSearchResponse response = spreadsheetService.search(
                    request.prompt(),
                    request.fileNames(),
                    request.effectiveMaxResults());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse("Folder error: " + e.getMessage()));
        }
    }

    // ─────────────────── POST /search/file  — single file deep search ────────

    @PostMapping("/search/file")
    @Operation(
        summary = "Search a single file by prompt (POST)",
        description = "Searches exactly one file and returns its matched rows plus a per-file AI summary."
    )
    public ResponseEntity<FileSearchResult> searchSingleFile(
            @Parameter(description = "File name, e.g. sales.xlsx", required = true)
            @RequestParam String fileName,

            @Parameter(description = "Natural-language search text", required = true)
            @RequestParam String prompt,

            @Parameter(description = "Max matching rows to return (default: 10)")
            @RequestParam(required = false, defaultValue = "10") int maxResults) {

        if (prompt.isBlank() || fileName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            SpreadsheetSearchResponse resp = spreadsheetService.search(
                    prompt, List.of(fileName), maxResults);
            if (resp.fileResults().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(resp.fileResults().get(0));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─────────────────── helper ───────────────────────────────────────────────

    private SpreadsheetSearchResponse errorResponse(String message) {
        return new SpreadsheetSearchResponse(message, Collections.emptyList(), 0, 0, 0);
    }
}
