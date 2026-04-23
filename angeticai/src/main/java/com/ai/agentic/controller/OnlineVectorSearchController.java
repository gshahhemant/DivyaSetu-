package com.ai.agentic.controller;

import com.ai.agentic.dto.IngestResponse;
import com.ai.agentic.dto.VectorSearchRequest;
import com.ai.agentic.dto.VectorSearchResponse;
import com.ai.agentic.service.OnlineVectorStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST API for the RAG (Retrieval-Augmented Generation) vector store powered by OpenAI.
 *
 * <p>Typical workflow:
 * <ol>
 *   <li>{@code POST /api/online-vector/ingest} — one-time call to index all spreadsheet rows
 *       using OpenAI embeddings (text-embedding-3-small).</li>
 *   <li>{@code POST /api/online-vector/search} — ask a question; get a gpt-4o answer plus matched rows.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/online-vector")
@Tag(name = "Online Vector Search (RAG + OpenAI)", description = "Embed spreadsheet data using OpenAI embeddings and perform semantic search with gpt-4o generated answers.")
public class OnlineVectorSearchController {

    private final OnlineVectorStoreService onlineVectorStoreService;

    public OnlineVectorSearchController(OnlineVectorStoreService onlineVectorStoreService) {
        this.onlineVectorStoreService = onlineVectorStoreService;
    }

    // ─────────────────────── Ingestion ────────────────────────────────────────

    @Operation(summary = "Ingest all spreadsheet files (OpenAI)",
               description = "Embed all rows from every file in C:/google_sheets using OpenAI embeddings and store in the vector store. Clears existing data first.")
    @PostMapping("/ingest")
    public ResponseEntity<IngestResponse> ingestAll() {
        try {
            IngestResponse response = onlineVectorStoreService.ingestAll();
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new IngestResponse("Ingestion failed: " + e.getMessage(),
                            0, 0, Collections.emptyList()));
        }
    }

    @Operation(summary = "Ingest a single spreadsheet file (OpenAI)",
               description = "Embed rows from one specific file using OpenAI embeddings and add to the vector store (without clearing existing entries).")
    @PostMapping("/ingest/{fileName}")
    public ResponseEntity<IngestResponse> ingestFile(@PathVariable String fileName) {
        try {
            IngestResponse response = onlineVectorStoreService.ingestFile(fileName);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new IngestResponse("Ingestion failed: " + e.getMessage(),
                            0, 0, Collections.emptyList()));
        }
    }

    // ─────────────────────── Management ───────────────────────────────────────

    @Operation(summary = "Vector store status (OpenAI)",
               description = "Returns how many segments are currently indexed and the source folder.")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "storeSize", onlineVectorStoreService.getStoreSize(),
                "folder",    "C:/google_sheets",
                "status",    onlineVectorStoreService.getStoreSize() > 0
                                ? "READY"
                                : "EMPTY — call POST /api/online-vector/ingest first"
        ));
    }

    @Operation(summary = "Clear the vector store (OpenAI)",
               description = "Removes all indexed embeddings. You will need to call /ingest again before searching.")
    @DeleteMapping("/store")
    public ResponseEntity<Map<String, Object>> clearStore() {
        onlineVectorStoreService.clearStore();
        return ResponseEntity.ok(Map.of("message", "Online vector store cleared."));
    }

    // ─────────────────────── Search ───────────────────────────────────────────

    /**
     * Semantic search powered by OpenAI embeddings + gpt-4o.
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
    @Operation(summary = "Semantic search (POST) — OpenAI",
               description = "Embed the query with OpenAI, retrieve the most similar spreadsheet rows, and generate a gpt-4o answer. Optionally filter by file names.")
    @PostMapping("/search")
    public ResponseEntity<VectorSearchResponse> search(@RequestBody VectorSearchRequest request) {
        if (request.query() == null || request.query().isBlank()) {
            return ResponseEntity.badRequest().body(
                    new VectorSearchResponse("Query must not be empty.",
                            Collections.emptyList(), request.query(), 0));
        }
        int max    = (request.maxResults() != null && request.maxResults() > 0)
                ? request.maxResults() : 5;
        List<String> filter = request.fileNames() != null ? request.fileNames() : Collections.emptyList();
        VectorSearchResponse response = onlineVectorStoreService.search(request.query(), max, filter);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Semantic search (GET) — OpenAI",
               description = "Quick GET-based semantic search using OpenAI. Use the POST endpoint for file filtering.")
    @GetMapping("/search")
    public ResponseEntity<VectorSearchResponse> searchGet(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int maxResults) {
        if (query.isBlank()) {
            return ResponseEntity.badRequest().body(
                    new VectorSearchResponse("Query must not be empty.",
                            Collections.emptyList(), query, 0));
        }
        VectorSearchResponse response = onlineVectorStoreService.search(query, maxResults, Collections.emptyList());
        return ResponseEntity.ok(response);
    }
}
