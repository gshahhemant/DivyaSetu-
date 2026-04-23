package com.ai.agentic.service;

import com.ai.agentic.dto.IngestResponse;
import com.ai.agentic.dto.FileIngestDetail;
import com.ai.agentic.dto.PdfDocumentMatch;
import com.ai.agentic.dto.PdfSearchResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RAG service for PDF and CSV documents stored in a local folder, backed by OpenAI.
 *
 * <p>Pipeline:
 * <ol>
 *   <li><b>Ingest</b>: extract text from every PDF (page-by-page) and CSV (row-by-row),
 *       embed each chunk with OpenAI (text-embedding-3-small), and store in an in-memory
 *       vector store. Subfolders are scanned recursively.</li>
 *   <li><b>Search</b>: embed the user query → find the most semantically similar chunks
 *       → pass them as context to the chat LLM → return an AI-generated answer together
 *       with the matching document locations.</li>
 * </ol>
 *
 * <p>Configuration key: {@code pdf.documents.folder}
 */
@Service
public class PdfDocumentService {

    private static final Logger log = LoggerFactory.getLogger(PdfDocumentService.class);

    /** Characters per text chunk when splitting long pages. */
    private static final int CHUNK_SIZE = 1000;

    @Value("${pdf.documents.folder}")
    private String pdfFolderPath;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String chatModelName;

    @Value("${openai.embedding-model:text-embedding-3-small}")
    private String embeddingModelName;

    @Value("${openai.timeout:60}")
    private int timeoutSeconds;

    private EmbeddingModel embeddingModel;
    private ChatLanguageModel chatModel;
    private InMemoryEmbeddingStore<TextSegment> store;
    private final AtomicInteger storeSize = new AtomicInteger(0);

    /** Caches embedded chunks per file so single-file re-ingestion doesn't create duplicates. */
    private final Map<String, List<CachedChunk>> embeddingCache = new LinkedHashMap<>();

    private record CachedChunk(Embedding embedding, TextSegment segment) {}

    /** DTO used only for JSON serialisation of the on-disk cache. */
    private record SerializedChunk(
            float[] embedding, String text,
            String fileName, String filePath,
            String chunkIndex, String pageNumber) {}

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(embeddingModelName)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(chatModelName)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        store = new InMemoryEmbeddingStore<>();
        log.info("PdfDocumentService ready. Folder: {}, ChatModel: {}, EmbeddingModel: {}",
                pdfFolderPath, chatModelName, embeddingModelName);
        loadCacheFromDisk();
    }

    // ─────────────────── public API ───────────────────────────────────────────

    /** Returns the number of chunks currently indexed. */
    public int getStoreSize() {
        return storeSize.get();
    }

    /** Returns the configured PDF folder path. */
    public String getPdfFolderPath() {
        return pdfFolderPath;
    }

    /** Clears all indexed data from the vector store, the embedding cache, and the on-disk cache file. */
    public void clearStore() {
        store = new InMemoryEmbeddingStore<>();
        embeddingCache.clear();
        storeSize.set(0);
        try {
            Files.deleteIfExists(cacheFilePath());
        } catch (Exception e) {
            log.warn("Could not delete cache file: {}", e.getMessage());
        }
        log.info("Document vector store cleared.");
    }

    /**
     * Recursively lists all PDF and CSV files found under the configured folder (including subfolders).
     * Returns relative paths from the root folder, e.g. {@code invoices/march-2024.pdf},
     * {@code data/contacts.csv}.
     */
    public List<String> listDocumentFiles() throws IOException {
        Path root = Path.of(pdfFolderPath);
        if (!Files.exists(root)) {
            throw new IOException("Documents folder not found: " + pdfFolderPath);
        }
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".pdf") || name.endsWith(".csv");
                    })
                    .map(p -> root.relativize(p).toString().replace('\\', '/'))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    /**
     * Ingests ALL PDF and CSV files from the configured folder (recursively).
     * Clears whatever was previously indexed.
     */
    public IngestResponse ingestAll() throws IOException {
        clearStore();
        List<String> files = listDocumentFiles();
        if (files.isEmpty()) {
            return new IngestResponse("No PDF or CSV files found in: " + pdfFolderPath,
                    0, 0, Collections.emptyList());
        }

        List<FileIngestDetail> details = new ArrayList<>();
        for (String fileName : files) {
            details.add(ingestFileInternal(fileName));
        }

        int total = details.stream().mapToInt(FileIngestDetail::rowsIngested).sum();
        IngestResponse response = new IngestResponse(
                String.format("Ingestion complete. %d file(s) processed, %d chunks indexed.",
                        files.size(), total),
                files.size(), total, details);
        saveCacheToDisk();
        return response;
    }

    /**
     * Ingests a single PDF or CSV file. If the file was previously indexed,
     * its old chunks are removed first so there are no duplicates.
     */
    public IngestResponse ingestFile(String fileName) throws IOException {
        // Remove old chunks for this file (if any) and rebuild store from remaining cache
        if (embeddingCache.containsKey(fileName)) {
            log.info("Re-ingesting '{}' — removing {} stale chunks first.", fileName, embeddingCache.get(fileName).size());
            embeddingCache.remove(fileName);
            rebuildStoreFromCache();
        }
        FileIngestDetail detail = ingestFileInternal(fileName);
        IngestResponse response = new IngestResponse(
                String.format("'%s' ingested: %d chunks indexed.", fileName, detail.rowsIngested()),
                1, detail.rowsIngested(), List.of(detail));
        saveCacheToDisk();
        return response;
    }

    /** Rebuilds the in-memory store from the cache (used after a single-file re-ingest). */
    private void rebuildStoreFromCache() {
        store = new InMemoryEmbeddingStore<>();
        int count = 0;
        for (List<CachedChunk> chunks : embeddingCache.values()) {
            for (CachedChunk c : chunks) {
                store.add(c.embedding(), c.segment());
                count++;
            }
        }
        storeSize.set(count);
        log.info("Store rebuilt from cache: {} chunks across {} file(s).", count, embeddingCache.size());
    }

    // ─────────────────── disk persistence ─────────────────────────────────────

    private Path cacheFilePath() {
        return Path.of(pdfFolderPath, ".document-cache.json");
    }

    /**
     * Saves the full embedding cache to {@code <documents-folder>/.document-cache.json}.
     * Called automatically after every ingest so the server can reload without re-ingesting.
     */
    private void saveCacheToDisk() {
        Path cacheFile = cacheFilePath();
        try {
            Map<String, List<SerializedChunk>> serial = new LinkedHashMap<>();
            embeddingCache.forEach((file, chunks) -> {
                List<SerializedChunk> sc = chunks.stream()
                        .map(c -> new SerializedChunk(
                                c.embedding().vector(),
                                c.segment().text(),
                                c.segment().metadata().getString("fileName"),
                                c.segment().metadata().getString("filePath"),
                                c.segment().metadata().getString("chunkIndex"),
                                c.segment().metadata().getString("pageNumber")))
                        .toList();
                serial.put(file, sc);
            });
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(cacheFile.toFile(), serial);
            log.info("Cache saved: {} file(s), {} chunks → {}", serial.size(), storeSize.get(), cacheFile);
        } catch (Exception e) {
            log.error("Failed to save cache to disk: {}", e.getMessage());
        }
    }

    /**
     * Loads the embedding cache from disk on startup.
     * If the file exists the vector store is rebuilt in memory with no OpenAI API calls.
     */
    private void loadCacheFromDisk() {
        Path cacheFile = cacheFilePath();
        if (!Files.exists(cacheFile)) {
            log.info("No cache file found — call POST /api/pdf/ingest to index documents.");
            return;
        }
        try {
            Map<String, List<SerializedChunk>> serial = objectMapper.readValue(
                    cacheFile.toFile(),
                    new TypeReference<Map<String, List<SerializedChunk>>>() {});

            for (Map.Entry<String, List<SerializedChunk>> entry : serial.entrySet()) {
                List<CachedChunk> chunks = new ArrayList<>();
                for (SerializedChunk sc : entry.getValue()) {
                    Embedding emb = Embedding.from(sc.embedding());
                    Metadata meta = new Metadata()
                            .put("fileName",   sc.fileName()   != null ? sc.fileName()   : "")
                            .put("filePath",   sc.filePath()   != null ? sc.filePath()   : "")
                            .put("chunkIndex", sc.chunkIndex() != null ? sc.chunkIndex() : "")
                            .put("pageNumber", sc.pageNumber() != null ? sc.pageNumber() : "");
                    TextSegment seg = TextSegment.from(sc.text(), meta);
                    chunks.add(new CachedChunk(emb, seg));
                }
                embeddingCache.put(entry.getKey(), chunks);
            }
            rebuildStoreFromCache();
            log.info("Cache loaded from disk: {} file(s), {} chunks — ready to search!", embeddingCache.size(), storeSize.get());
        } catch (Exception e) {
            log.error("Failed to load cache from disk ({}): {} — starting fresh.", cacheFile, e.getMessage());
            embeddingCache.clear();
            store = new InMemoryEmbeddingStore<>();
            storeSize.set(0);
        }
    }

    /**
     * Embeds the query and finds the most semantically similar PDF pages.
     * An AI-generated answer is synthesised from the top matching chunks.
     *
     * @param query      natural-language question / search text
     * @param maxResults maximum number of matching chunks to return
     */
    public PdfSearchResponse search(String query, int maxResults) {
        if (storeSize.get() == 0) {
            return new PdfSearchResponse(
                    "The document store is empty. Please call POST /api/pdf/ingest first.",
                    Collections.emptyList(), query, 0);
        }

        // 1. Embed the query
        Embedding queryEmbedding;
        try {
            queryEmbedding = embeddingModel.embed(query).content();
        } catch (Exception e) {
            log.error("Failed to embed query: {}", e.getMessage());
            return new PdfSearchResponse("Embedding error: " + e.getMessage(),
                    Collections.emptyList(), query, 0);
        }

        // 2. Retrieve top candidates
        List<EmbeddingMatch<TextSegment>> matches =
                store.search(dev.langchain4j.store.embedding.EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(maxResults)
                        .build()).matches();

        // 3. Map to DTO
        List<PdfDocumentMatch> docMatches = matches.stream()
                .map(this::toDocumentMatch)
                .collect(Collectors.toList());

        // 4. Generate AI answer from retrieved context
        String answer = generateAnswer(query, docMatches);

        return new PdfSearchResponse(answer, docMatches, query, docMatches.size());
    }

    // ─────────────────── private helpers ──────────────────────────────────────

    /**
     * @param relativePath relative path from the root folder, e.g. {@code invoices/march.pdf}
     *                     or {@code data/records.csv}
     */
    private FileIngestDetail ingestFileInternal(String relativePath) {
        Path filePath = Path.of(pdfFolderPath).resolve(relativePath);
        if (!Files.exists(filePath)) {
            return new FileIngestDetail(relativePath, 0, "File not found: " + filePath);
        }

        boolean isCsv = relativePath.toLowerCase().endsWith(".csv");
        List<String> chunks;
        try {
            chunks = isCsv
                    ? extractCsvChunks(filePath, relativePath)
                    : extractChunks(filePath.toFile(), relativePath);
        } catch (Exception e) {
            log.error("Cannot parse file {}: {}", relativePath, e.getMessage());
            return new FileIngestDetail(relativePath, 0, "ERROR: " + e.getMessage());
        }

        if (chunks.isEmpty()) {
            return new FileIngestDetail(relativePath, 0, "File has no extractable content.");
        }

        // Build all TextSegment objects up-front
        List<TextSegment> segments = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            Metadata meta = new Metadata()
                    .put("fileName",   relativePath)
                    .put("filePath",   filePath.toString())
                    .put("chunkIndex", String.valueOf(i + 1))
                    .put("pageNumber", extractPageNumber(chunkText, i));
            segments.add(TextSegment.from(chunkText, meta));
        }

        // Embed in batches to avoid per-chunk API call overhead and timeouts
        final int EMBED_BATCH_SIZE = 100;
        int ingested = 0;
        List<CachedChunk> fileChunks = new ArrayList<>(segments.size());
        for (int batchStart = 0; batchStart < segments.size(); batchStart += EMBED_BATCH_SIZE) {
            int batchEnd = Math.min(batchStart + EMBED_BATCH_SIZE, segments.size());
            List<TextSegment> batch = segments.subList(batchStart, batchEnd);
            try {
                Response<List<Embedding>> response = embeddingModel.embedAll(batch);
                List<Embedding> embeddings = response.content();
                for (int j = 0; j < batch.size(); j++) {
                    store.add(embeddings.get(j), batch.get(j));
                    fileChunks.add(new CachedChunk(embeddings.get(j), batch.get(j)));
                    storeSize.incrementAndGet();
                    ingested++;
                }
                log.debug("Embedded batch {}-{} of {} for: {}", batchStart + 1, batchEnd, segments.size(), relativePath);
            } catch (Exception e) {
                log.error("Embed failed for batch {}-{} in {}: {}", batchStart + 1, batchEnd, relativePath, e.getMessage());
            }
        }
        embeddingCache.put(relativePath, fileChunks);

        log.info("Ingested {}/{} chunks from: {}", ingested, chunks.size(), relativePath);
        return new FileIngestDetail(relativePath, ingested, ingested > 0 ? "OK" : "All chunks failed to embed.");
    }

    /**
     * Reads a CSV file and converts every row into a text chunk with a
     * "[File: X, Row: N]" prefix so row number is stored in metadata.
     *
     * <p>Uses a lenient format (EXCEL-style quoting, ignore surrounding spaces,
     * backslash escape) so common real-world CSV quirks are handled gracefully.
     * Malformed individual rows are skipped with a warning rather than aborting.
     */
    private List<String> extractCsvChunks(Path csvFile, String displayName) throws IOException {
        List<String> chunks = new ArrayList<>();

        // Try UTF-8 first; fall back to ISO-8859-1 if the file has non-UTF chars
        java.nio.charset.Charset charset = StandardCharsets.UTF_8;
        try {
            Files.newBufferedReader(csvFile, StandardCharsets.UTF_8).close();
        } catch (java.nio.charset.MalformedInputException e) {
            charset = java.nio.charset.StandardCharsets.ISO_8859_1;
        }

        CSVFormat format = CSVFormat.EXCEL          // lenient quoting (tolerates extra chars after quote)
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true)
                .setTrim(true)
                .setEscape('\\')                    // allow backslash-escaped chars inside fields
                .build();

        try (Reader reader = Files.newBufferedReader(csvFile, charset);
             org.apache.commons.csv.CSVParser parser = format.parse(reader)) {

            for (CSVRecord record : parser) {
                try {
                    if (!record.isConsistent()) {
                        log.warn("CSV [{}] row {} has inconsistent column count — skipping", displayName, parser.getCurrentLineNumber());
                        continue;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("[File: ").append(displayName)
                      .append(", Row: ").append(parser.getCurrentLineNumber()).append("] ");
                    record.toMap().forEach((k, v) -> sb.append(k).append(": ").append(v).append("; "));
                    String chunk = sb.toString().trim();
                    if (!chunk.isEmpty()) {
                        chunks.add(chunk);
                    }
                } catch (Exception rowEx) {
                    log.warn("CSV [{}] row {} skipped due to error: {}", displayName, parser.getCurrentLineNumber(), rowEx.getMessage());
                }
            }
        }

        log.info("Extracted {} row chunks from CSV: {}", chunks.size(), displayName);
        return chunks;
    }

    /**
     * Extracts text from each page of a PDF and splits long pages into
     * fixed-size chunks so individual embeddings stay within model limits.
     * Each chunk is prefixed with "[File: X, Page: N]" metadata text.
     *
     * @param pdfFile      the actual PDF file on disk
     * @param displayName  relative path used in the prefix, e.g. invoices/march.pdf
     */
    private List<String> extractChunks(File pdfFile, String displayName) throws IOException {
        List<String> chunks = new ArrayList<>();
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int numPages = doc.getNumberOfPages();
            for (int page = 1; page <= numPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(doc).trim();
                if (pageText.isEmpty()) continue;

                List<String> pageChunks = splitIntoChunks(pageText, CHUNK_SIZE);
                for (String chunk : pageChunks) {
                    String prefix = "[File: " + displayName + ", Page: " + page + "] ";
                    chunks.add(prefix + chunk);
                }
            }
        }
        return chunks;
    }

    private List<String> splitIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            // Try to break at a word boundary
            if (end < text.length()) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }
            chunks.add(text.substring(start, end).trim());
            start = end + 1;
        }
        return chunks;
    }

    /** Extracts page number from a chunk's "[File: ..., Page: N]" prefix, or returns chunkIndex+1. */
    private String extractPageNumber(String chunkText, int fallbackIndex) {
        if (chunkText.startsWith("[File:")) {
            int pageStart = chunkText.indexOf("Page: ");
            if (pageStart >= 0) {
                int pageEnd = chunkText.indexOf(']', pageStart);
                if (pageEnd > pageStart) {
                    return chunkText.substring(pageStart + 6, pageEnd).trim();
                }
            }
        }
        return String.valueOf(fallbackIndex + 1);
    }

    private PdfDocumentMatch toDocumentMatch(EmbeddingMatch<TextSegment> match) {
        TextSegment seg = match.embedded();
        String fileName  = getMetaString(seg, "fileName");
        String filePath  = getMetaString(seg, "filePath");
        int    pageNum   = parseIntSafe(getMetaString(seg, "pageNumber"), 0);
        String snippet   = seg.text();
        // Trim the internal "[File: ..., Page: N]" prefix for display
        int bracketEnd = snippet.indexOf("] ");
        if (bracketEnd >= 0 && bracketEnd < 60) {
            snippet = snippet.substring(bracketEnd + 2);
        }
        // Truncate very long snippets
        if (snippet.length() > 400) {
            snippet = snippet.substring(0, 400) + "…";
        }
        return new PdfDocumentMatch(fileName, filePath, pageNum, snippet, match.score());
    }

    private String generateAnswer(String query, List<PdfDocumentMatch> matches) {
        if (matches.isEmpty()) {
            return "No relevant content found in the PDF documents for the given query.";
        }

        StringBuilder context = new StringBuilder();
        Set<String> seenFiles = new LinkedHashSet<>();
        for (PdfDocumentMatch m : matches) {
            context.append("[").append(m.fileName()).append(", page ").append(m.pageNumber()).append("]\n");
            context.append(m.textSnippet()).append("\n\n");
            seenFiles.add(m.fileName());
        }

        String prompt = """
                You are a document search assistant. Based only on the PDF excerpts provided below, \
                answer the user's question. Also mention which document(s) contain the relevant information.

                User question: %s

                PDF excerpts:
                %s

                Answer:""".formatted(query, context);

        try {
            return chatModel.generate(prompt).trim();
        } catch (Exception e) {
            log.error("Chat model error: {}", e.getMessage());
            String fileList = String.join(", ", seenFiles);
            return "Relevant content found in: " + fileList + ". (AI answer unavailable: " + e.getMessage() + ")";
        }
    }

    private String getMetaString(TextSegment seg, String key) {
        String value = seg.metadata().getString(key);
        return value != null ? value : "";
    }

    private int parseIntSafe(String value, int defaultVal) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
