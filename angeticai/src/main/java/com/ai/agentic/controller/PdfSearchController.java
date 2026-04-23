package com.ai.agentic.controller;

import com.ai.agentic.dto.IngestResponse;
import com.ai.agentic.dto.PdfSearchRequest;
import com.ai.agentic.dto.PdfSearchResponse;
import com.ai.agentic.service.PdfDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST API for searching PDF and CSV documents using semantic (vector) search and AI-generated answers.
 *
 * <p>Typical workflow:
 * <ol>
 *   <li>{@code POST /api/pdf/ingest} — index all PDFs and CSVs in the configured folder (one-time setup).</li>
 *   <li>{@code POST /api/pdf/search} — send a natural-language prompt; receive an AI answer plus
 *       links to the matching documents and pages/rows.</li>
 * </ol>
 *
 * <p>Documents folder is configured via {@code pdf.documents.folder} in {@code application.yml}.
 */
@RestController
@RequestMapping("/api/pdf")
@Tag(
    name = "PDF & CSV Document Search",
    description = """
        Indexes PDF and CSV documents from a local folder (including subfolders) into a vector store
        and enables semantic search with AI-generated answers.
        
        Workflow:
        1. POST /api/pdf/ingest  — extract & embed all PDFs and CSVs (call once, or after new files are added)
        2. POST /api/pdf/search  — search with a natural-language prompt
        """
)
public class PdfSearchController {

    private final PdfDocumentService pdfDocumentService;

    public PdfSearchController(PdfDocumentService pdfDocumentService) {
        this.pdfDocumentService = pdfDocumentService;
    }

    // ─────────────────────── File listing ─────────────────────────────────────

    @Operation(
        summary = "List PDF and CSV files",
        description = "Returns the relative paths of all .pdf and .csv files found in the configured documents folder (including subfolders)."
    )
    @GetMapping("/files")
    public ResponseEntity<List<String>> listFiles() {
        try {
            return ResponseEntity.ok(pdfDocumentService.listDocumentFiles());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─────────────────────── Ingestion ────────────────────────────────────────

    @Operation(
        summary = "Ingest all PDF and CSV files",
        description = """
            Recursively reads every .pdf and .csv file from the configured folder,
            embeds each page/row chunk with OpenAI (text-embedding-3-small), and stores
            the embeddings in the in-memory vector store. Any previously indexed data is cleared first.
            
            - PDF files: chunked page-by-page
            - CSV files: chunked row-by-row
            
            Call this once after startup, or whenever new files are added to the folder.
            """
    )
    @PostMapping("/ingest")
    public ResponseEntity<IngestResponse> ingestAll() {
        try {
            IngestResponse response = pdfDocumentService.ingestAll();
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new IngestResponse("Ingestion failed: " + e.getMessage(),
                            0, 0, Collections.emptyList()));
        }
    }

    @Operation(
        summary = "Ingest a single PDF or CSV file",
        description = "Extracts and embeds content from one specific file and adds it to the vector store " +
                      "without clearing data from other files. " +
                      "Pass the relative path from the documents root, e.g. invoices/march-2024.pdf or data/records.csv"
    )
    @PostMapping("/ingest/file")
    public ResponseEntity<IngestResponse> ingestFile(
            @Parameter(description = "Relative path to the PDF from the documents root, e.g. invoices/report.pdf")
            @RequestParam String filePath) {
        try {
            IngestResponse response = pdfDocumentService.ingestFile(filePath);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new IngestResponse("Ingestion failed: " + e.getMessage(),
                            0, 0, Collections.emptyList()));
        }
    }

    // ─────────────────────── Management ───────────────────────────────────────

    @Operation(
        summary = "Vector store status",
        description = "Returns how many PDF/CSV chunks are currently indexed along with the source folder path."
    )
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        int size = pdfDocumentService.getStoreSize();
        return ResponseEntity.ok(Map.of(
                "chunksIndexed", size,
                "folder",        pdfDocumentService.getPdfFolderPath(),
                "status",        size > 0 ? "READY" : "EMPTY — call POST /api/pdf/ingest first"
        ));
    }

    @Operation(
        summary = "Clear the document vector store",
        description = "Removes all indexed PDF/CSV embeddings. You will need to call POST /api/pdf/ingest again before searching."
    )
    @DeleteMapping("/store")
    public ResponseEntity<Map<String, Object>> clearStore() {
        pdfDocumentService.clearStore();
        return ResponseEntity.ok(Map.of("message", "PDF vector store cleared."));
    }

    // ─────────────────────── Search ───────────────────────────────────────────

    @Operation(
        summary = "Search PDF documents by prompt (POST)",
        description = """
            Embeds the query, retrieves the most semantically similar PDF page chunks,
            and generates an AI answer that cites the source documents and pages.
            
            Example request body:
            ```json
            {
              "query": "invoice for laptop purchase March 2024",
              "maxResults": 5
            }
            ```
            
            The response includes:
            - An AI-generated answer mentioning which document(s) to look at.
            - A `matches` list showing file name, page number, text snippet and similarity score.
            """
    )
    @PostMapping("/search")
    public ResponseEntity<PdfSearchResponse> search(@RequestBody PdfSearchRequest request) {
        if (request.query() == null || request.query().isBlank()) {
            return ResponseEntity.badRequest().body(
                    new PdfSearchResponse("Query must not be empty.",
                            Collections.emptyList(), request.query(), 0));
        }
        int max = (request.maxResults() != null && request.maxResults() > 0)
                ? request.maxResults() : 5;
        PdfSearchResponse response = pdfDocumentService.search(request.query(), max);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Search PDF documents by prompt (GET)",
        description = "Quick GET search. Pass ?query= with your natural-language prompt."
    )
    @GetMapping("/search")
    public ResponseEntity<PdfSearchResponse> searchGet(
            @Parameter(description = "Natural-language search query", required = true)
            @RequestParam String query,

            @Parameter(description = "Max number of matching chunks to return (default: 5)")
            @RequestParam(required = false, defaultValue = "5") int maxResults) {

        if (query.isBlank()) {
            return ResponseEntity.badRequest().body(
                    new PdfSearchResponse("Query must not be empty.",
                            Collections.emptyList(), query, 0));
        }
        PdfSearchResponse response = pdfDocumentService.search(query, maxResults);
        return ResponseEntity.ok(response);
    }
}
