package com.ai.agentic.service;

import com.ai.agentic.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) service.
 *
 * <p>Pipeline:
 * <ol>
 *   <li>Ingest: read every row from each .xlsx/.csv file in C:/google_sheets,
 *       convert to a text segment, embed with Ollama (nomic-embed-text), and
 *       store in an in-memory vector store.</li>
 *   <li>Search: embed the user query → find the most semantically similar rows
 *       → pass them as context to the chat LLM → return a natural-language answer.</li>
 * </ol>
 *
 * <p>Note: The vector store is in-memory and rebuilt on each call to
 * {@link #ingestAll()}. Call that endpoint once after startup (or whenever
 * the files in C:/google_sheets change).
 *
 * <p>Requires Ollama models:
 * <ul>
 *   <li>{@code ollama pull nomic-embed-text} — for embeddings</li>
 *   <li>Your chat model (e.g. {@code gemma3:12b}) — for generating the answer</li>
 * </ul>
 */
@Service
public class VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);

    private final LocalSpreadsheetService spreadsheetService;
    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String chatModelName;

    @Value("${ollama.embedding-model:nomic-embed-text}")
    private String embeddingModelName;

    @Value("${ollama.timeout:120}")
    private int timeoutSeconds;

    private EmbeddingModel     embeddingModel;
    private ChatLanguageModel  chatModel;
    private InMemoryEmbeddingStore<TextSegment> store;
    private final AtomicInteger storeSize = new AtomicInteger(0);

    public VectorStoreService(LocalSpreadsheetService spreadsheetService,
                               ObjectMapper objectMapper) {
        this.spreadsheetService = spreadsheetService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(embeddingModelName)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        chatModel = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(chatModelName)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        store = new InMemoryEmbeddingStore<>();
        log.info("VectorStoreService ready. Chat model: {}, Embedding model: {}",
                chatModelName, embeddingModelName);
    }

    // ─────────────────── public API ───────────────────────────────────────────

    /** Number of segments currently in the vector store. */
    public int getStoreSize() {
        return storeSize.get();
    }

    /** Clears the vector store (all indexed data is lost). */
    public void clearStore() {
        store = new InMemoryEmbeddingStore<>();
        storeSize.set(0);
        log.info("Vector store cleared.");
    }

    /**
     * Reads ALL .xlsx/.csv files from the configured folder, embeds every row,
     * and stores them in the vector store (previous contents are cleared first).
     */
    public IngestResponse ingestAll() throws IOException {
        clearStore();
        List<String> files = spreadsheetService.listFiles();
        if (files.isEmpty()) {
            return new IngestResponse("No spreadsheet files found in the configured folder.",
                    0, 0, Collections.emptyList());
        }

        List<FileIngestDetail> details = new ArrayList<>();
        for (String fileName : files) {
            details.add(ingestFileInternal(fileName));
        }

        int total = details.stream().mapToInt(FileIngestDetail::rowsIngested).sum();
        return new IngestResponse(
                String.format("Ingestion complete. %d file(s) processed, %d rows indexed.",
                        files.size(), total),
                files.size(), total, details);
    }

    /**
     * Embeds all rows from a single file and adds them to the store
     * (does NOT clear existing data from other files).
     */
    public IngestResponse ingestFile(String fileName) throws IOException {
        FileIngestDetail detail = ingestFileInternal(fileName);
        return new IngestResponse(
                String.format("File '%s' ingested: %d rows indexed.", fileName, detail.rowsIngested()),
                1, detail.rowsIngested(), List.of(detail));
    }

    /**
     * Embeds the query, retrieves the most similar rows from the vector store,
     * then asks the chat LLM to synthesise a natural-language answer.
     *
     * @param query          the user's question / search text
     * @param maxResults     how many top-matching rows to retrieve
     * @param fileNameFilter optional list of file names to restrict the search to
     */
    public VectorSearchResponse search(String query,
                                       int maxResults,
                                       List<String> fileNameFilter) {
        if (storeSize.get() == 0) {
            return new VectorSearchResponse(
                    "The vector store is empty. Please call POST /api/vector/ingest first.",
                    Collections.emptyList(), query, 0);
        }

        // 1. Embed the query
        Embedding queryEmbedding;
        try {
            queryEmbedding = embeddingModel.embed(query).content();
        } catch (Exception e) {
            log.error("Failed to embed query: {}", e.getMessage());
            return new VectorSearchResponse("Embedding error: " + e.getMessage(),
                    Collections.emptyList(), query, 0);
        }

        // 2. Retrieve top candidates (fetch extra so file-filter doesn't reduce results too much)
        int fetchCount = (fileNameFilter != null && !fileNameFilter.isEmpty())
                ? maxResults * 5 : maxResults;
        List<EmbeddingMatch<TextSegment>> matches =
                store.search(dev.langchain4j.store.embedding.EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(fetchCount)
                        .build()).matches();

        // 3. Apply optional file name filter
        if (fileNameFilter != null && !fileNameFilter.isEmpty()) {
            matches = matches.stream()
                    .filter(m -> {
                        String fn = m.embedded().metadata().getString("fileName");
                        return fn != null && fileNameFilter.contains(fn);
                    })
                    .limit(maxResults)
                    .collect(Collectors.toList());
        }

        // 4. Map to DTO
        List<SegmentMatch> segments = matches.stream()
                .map(this::toSegmentMatch)
                .collect(Collectors.toList());

        // 5. Generate AI answer from retrieved context
        String answer = generateAnswer(query, segments);

        return new VectorSearchResponse(answer, segments, query, segments.size());
    }

    // ─────────────────── private helpers ──────────────────────────────────────

    private FileIngestDetail ingestFileInternal(String fileName) {
        List<Map<String, String>> rows;
        try {
            rows = spreadsheetService.readAllRows(fileName);
        } catch (Exception e) {
            log.error("Cannot read file {}: {}", fileName, e.getMessage());
            return new FileIngestDetail(fileName, 0, "ERROR: " + e.getMessage());
        }

        if (rows.isEmpty()) {
            return new FileIngestDetail(fileName, 0, "File is empty — no rows to index.");
        }

        int ingested = 0;
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            String rowText   = buildRowText(fileName, i + 1, row);
            String rowDataJson = toJson(row);

            Metadata meta = new Metadata()
                    .put("fileName",  fileName)
                    .put("rowNumber", String.valueOf(i + 1))
                    .put("rowData",   rowDataJson);

            TextSegment segment = TextSegment.from(rowText, meta);
            try {
                Embedding embedding = embeddingModel.embed(rowText).content();
                store.add(embedding, segment);
                storeSize.incrementAndGet();
                ingested++;
            } catch (Exception e) {
                log.error("Embed failed for row {} in {}: {}", i + 1, fileName, e.getMessage());
            }
        }

        log.info("Ingested {}/{} rows from {}", ingested, rows.size(), fileName);
        return new FileIngestDetail(fileName, ingested, ingested > 0 ? "OK" : "All rows failed to embed.");
    }

    private String buildRowText(String fileName, int rowNum, Map<String, String> row) {
        StringBuilder sb = new StringBuilder();
        sb.append("[File: ").append(fileName).append(", Row: ").append(rowNum).append("] ");
        row.forEach((k, v) -> sb.append(k).append(": ").append(v).append("; "));
        return sb.toString().trim();
    }

    private SegmentMatch toSegmentMatch(EmbeddingMatch<TextSegment> match) {
        TextSegment seg = match.embedded();
        String fileName  = getString(seg, "fileName");
        String rowNumStr = getString(seg, "rowNumber");
        String rowDataJson = getString(seg, "rowData");

        Map<String, String> rowData = parseJson(rowDataJson);
        int rowNum = 0;
        try { rowNum = Integer.parseInt(rowNumStr); } catch (Exception ignored) {}

        return new SegmentMatch(fileName, rowNum, seg.text(), match.score(), rowData);
    }

    private String getString(TextSegment seg, String key) {
        String val = seg.metadata().getString(key);
        return val != null ? val : "";
    }

    private String generateAnswer(String query, List<SegmentMatch> segments) {
        if (segments.isEmpty()) {
            return "No relevant data found in the vector store for your query.";
        }
        StringBuilder context = new StringBuilder();
        for (SegmentMatch s : segments) {
            context.append(s.content()).append("\n");
        }
        String prompt = String.format("""
                You are a helpful assistant. Using ONLY the spreadsheet data below,
                answer the user's question as clearly and concisely as possible.

                RETRIEVED SPREADSHEET DATA:
                %s

                USER QUESTION: %s

                Provide a direct, factual answer based solely on the data above.
                """, context, query);
        try {
            return chatModel.generate(prompt).trim();
        } catch (Exception e) {
            log.error("Chat model failed: {}", e.getMessage());
            return "Retrieved " + segments.size() + " relevant row(s). See segments below.";
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private Map<String, String> parseJson(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, String>>() {});
        } catch (Exception e) {
            return Collections.singletonMap("raw", json);
        }
    }
}
