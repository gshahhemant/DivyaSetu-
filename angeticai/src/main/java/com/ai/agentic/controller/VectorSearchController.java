package com.ai.agentic.controller;

import com.ai.agentic.dto.IngestResponse;
import com.ai.agentic.dto.VectorSearchRequest;
import com.ai.agentic.dto.VectorSearchResponse;
import com.ai.agentic.service.VectorStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST API for the RAG (Retrieval-Augmented Generation) vector store.
 *
 * <p>Typical workflow:
 * <ol>
 *   <li>{@code POST /api/vector/ingest} — one-time call to index all spreadsheet rows.</li>
 *   <li>{@code POST /api/vector/search} — ask a question; get an AI answer plus matched rows.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/vector")
@Tag(name = "Vector Search (RAG)", description = "Embed spreadsheet data into a vector store and perform semantic search with AI-generated answers.")
public class VectorSearchController {

    private final VectorStoreService vectorStoreService;

    public VectorSearchController(VectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }

    // ─────────────────────── Ingestion ────────────────────────────────────────

    /**
     * Reads every .xlsx/.csv file from C:/google_sheets, embeds each row, and
     * stores the embeddings in the in-memory vector store.
     * Any previously indexed data is cleared first.
     *
     * <p>Call this once after startup (or whenever files change).
     * Depending on the number of rows this can take a few minutes.
     */
    @Operation(summary = "Ingest all spreadsheet files",
               description = "Embed all rows from every file in C:/google_sheets and store in the vector store. Clears existing data first.")
    @PostMapping("/ingest")
    public ResponseEntity<IngestResponse> ingestAll() {
        try {
            IngestResponse response = vectorStoreService.ingestAll();
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new IngestResponse("Ingestion failed: " + e.getMessage(),
                            0, 0, Collections.emptyList()));
        }
    }

    /**
     * Embeds all rows from a single file and adds them to the store.
     * Does NOT clear other files' data.
     */
    @Operation(summary = "Ingest a single spreadsheet file",
               description = "Embed rows from one specific file and add to the vector store (without clearing existing entries).")
    @PostMapping("/ingest/{fileName}")
    public ResponseEntity<IngestResponse> ingestFile(@PathVariable String fileName) {
        try {
            IngestResponse response = vectorStoreService.ingestFile(fileName);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new IngestResponse("Ingestion failed: " + e.getMessage(),
                            0, 0, Collections.emptyList()));
        }
    }

    // ─────────────────────── Management ───────────────────────────────────────

    /**
     * Returns the current state of the vector store (size, folder path).
     */
    @Operation(summary = "Vector store status",
               description = "Returns how many segments are currently indexed and the source folder.")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "storeSize", vectorStoreService.getStoreSize(),
                "folder",    "C:/google_sheets",
                "status",    vectorStoreService.getStoreSize() > 0 ? "READY" : "EMPTY — call POST /ingest first"
        ));
    }

    /**
     * Clears ALL indexed data from the vector store.
     */
    @Operation(summary = "Clear the vector store",
               description = "Removes all indexed embeddings. You will need to call /ingest again before searching.")
    @DeleteMapping("/store")
    public ResponseEntity<Map<String, Object>> clearStore() {
        vectorStoreService.clearStore();
        return ResponseEntity.ok(Map.of("message", "Vector store cleared."));
    }

    // ─────────────────────── Search ───────────────────────────────────────────

    /**
     * Semantic search with AI-generated answer.
     *
     * <p>The query is embedded, the most similar rows are retrieved, and the chat
     * LLM synthesises a natural-language answer from those rows.
     *
     * <p>Request body:
     * <pre>{@code
     * {
     *   "query": "Which products have pending status?",
     *   "maxResults": 5,
     *   "fileNames": ["sales.xlsx"]   // optional file filter
     * }
     * }</pre>
     */
    @Operation(summary = "Semantic search (POST)",
               description = "Embed the query, retrieve the most similar spreadsheet rows, and generate an AI answer. Optionally filter by file names.")
    @PostMapping("/search")
    public ResponseEntity<VectorSearchResponse> search(@RequestBody VectorSearchRequest request) {
        if (request.query() == null || request.query().isBlank()) {
            return ResponseEntity.badRequest().body(
                    new VectorSearchResponse("Query must not be empty.",
                            Collections.emptyList(), request.query(), 0));
        }
        int max = (request.maxResults() != null && request.maxResults() > 0)
                ? request.maxResults() : 5;
        List<String> filter = request.fileNames() != null ? request.fileNames() : Collections.emptyList();
        VectorSearchResponse response = vectorStoreService.search(request.query(), max, filter);
        return ResponseEntity.ok(response);
    }

    /**
     * Convenience GET endpoint for quick searches without a request body.
     *
     * <p>Example: {@code GET /api/vector/search?query=pending+orders&maxResults=5}
     */
    @Operation(summary = "Semantic search (GET)",
               description = "Quick GET-based semantic search. Use the POST endpoint for file filtering.")
    @GetMapping("/search")
    public ResponseEntity<VectorSearchResponse> searchGet(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int maxResults) {
        if (query.isBlank()) {
            return ResponseEntity.badRequest().body(
                    new VectorSearchResponse("Query must not be empty.",
                            Collections.emptyList(), query, 0));
        }
        VectorSearchResponse response = vectorStoreService.search(query, maxResults, Collections.emptyList());
        return ResponseEntity.ok(response);
    }
}
