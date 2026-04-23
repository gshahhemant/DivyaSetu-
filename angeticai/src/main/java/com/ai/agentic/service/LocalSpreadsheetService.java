package com.ai.agentic.service;

import com.ai.agentic.dto.FileSearchResult;
import com.ai.agentic.dto.SpreadsheetSearchResponse;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads .xlsx and .csv files from a local folder and uses the Ollama AI model
 * to find rows that match a natural-language search prompt.
 *
 * <p>Supported file formats:
 * <ul>
 *   <li><b>.xlsx</b> — reads the first sheet tab</li>
 *   <li><b>.csv</b>  — reads the whole file; first row treated as header</li>
 * </ul>
 *
 * <p>Configuration (application.yml):
 * <pre>
 * local:
 *   spreadsheets:
 *     folder: C:/google_sheets
 * </pre>
 */
@Service
public class LocalSpreadsheetService {

    private static final Logger log = LoggerFactory.getLogger(LocalSpreadsheetService.class);

    @Value("${local.spreadsheets.folder}")
    private String folderPath;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    @Value("${ollama.timeout:120}")
    private int ollamaTimeoutSeconds;

    private ChatLanguageModel chatModel;

    @PostConstruct
    public void init() {
        chatModel = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModel)
                .timeout(Duration.ofSeconds(ollamaTimeoutSeconds))
                .build();
        log.info("LocalSpreadsheetService initialised. Folder: {}", folderPath);
    }

    // ─────────────────────── public API ───────────────────────────────────────

    /**
     * Returns the names of all .xlsx and .csv files found in the configured folder.
     */
    public List<String> listFiles() throws IOException {
        Path folder = Path.of(folderPath);
        if (!Files.exists(folder)) {
            throw new IOException("Spreadsheet folder not found: " + folderPath);
        }
        try (Stream<Path> stream = Files.list(folder)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> name.toLowerCase().endsWith(".xlsx")
                                 || name.toLowerCase().endsWith(".csv"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    /**
     * Reads all rows from a single file as a list of maps (column header → cell value).
     *
     * @param fileName file name only (e.g. "sales.xlsx"), not a full path
     */
    public List<Map<String, String>> readAllRows(String fileName) throws IOException {
        Path file = Path.of(folderPath, fileName);
        if (!Files.exists(file)) {
            throw new IOException("File not found: " + file);
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".xlsx")) {
            return readXlsx(file);
        } else if (lower.endsWith(".csv")) {
            return readCsv(file);
        }
        throw new IllegalArgumentException("Unsupported file type: " + fileName);
    }

    /**
     * Searches the given prompt across one or more files.
     * If {@code fileNames} is empty/null, all files in the folder are searched.
     */
    public SpreadsheetSearchResponse search(String prompt, List<String> fileNames, int maxResultsPerFile)
            throws IOException {

        List<String> targets = (fileNames != null && !fileNames.isEmpty())
                ? fileNames
                : listFiles();

        if (targets.isEmpty()) {
            return new SpreadsheetSearchResponse(
                    "No spreadsheet files found in folder: " + folderPath,
                    Collections.emptyList(), 0, 0, 0);
        }

        List<FileSearchResult> fileResults = new ArrayList<>();
        for (String fileName : targets) {
            fileResults.add(searchOneFile(prompt, fileName, maxResultsPerFile));
        }

        int filesWithMatches = (int) fileResults.stream().filter(r -> r.matchedCount() > 0).count();
        int totalMatched     = fileResults.stream().mapToInt(FileSearchResult::matchedCount).sum();
        String overall       = buildOverallSummary(prompt, fileResults);

        return new SpreadsheetSearchResponse(overall, fileResults,
                fileResults.size(), filesWithMatches, totalMatched);
    }

    // ─────────────────────── private helpers ──────────────────────────────────

    private FileSearchResult searchOneFile(String prompt, String fileName, int maxResults) {
        List<Map<String, String>> allRows;
        try {
            allRows = readAllRows(fileName);
        } catch (Exception e) {
            log.error("Failed to read file {}: {}", fileName, e.getMessage());
            return new FileSearchResult(fileName,
                    "Error reading file: " + e.getMessage(),
                    Collections.emptyList(), 0, 0);
        }

        if (allRows.isEmpty()) {
            return new FileSearchResult(fileName, "File is empty or has no data rows.",
                    Collections.emptyList(), 0, 0);
        }

        // Build compact numbered text for the LLM
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allRows.size(); i++) {
            sb.append("Row ").append(i + 1).append(": ");
            allRows.get(i).forEach((k, v) -> sb.append(k).append("=").append(v).append("; "));
            sb.append("\n");
        }

        String llmReply = chatModel.generate(buildMatchPrompt(prompt, sb.toString(), maxResults)).trim();
        log.info("[{}] LLM reply: {}", fileName, llmReply);

        List<Integer> indexes = parseRowNumbers(llmReply, allRows.size());
        String        summary = extractSummary(llmReply);

        List<Map<String, String>> matched = indexes.stream()
                .map(idx -> allRows.get(idx - 1))
                .limit(maxResults)
                .collect(Collectors.toList());

        return new FileSearchResult(fileName, summary, matched, allRows.size(), matched.size());
    }

    // ── file readers ──────────────────────────────────────────────────────────

    private List<Map<String, String>> readXlsx(Path file) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (InputStream is = Files.newInputStream(file);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return rows;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cellToString(cell));
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    rowMap.put(headers.get(c), cell == null ? "" : cellToString(cell));
                }
                rows.add(rowMap);
            }
        }
        log.info("Read {} rows from XLSX: {}", rows.size(), file.getFileName());
        return rows;
    }

    private List<Map<String, String>> readCsv(Path file) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            for (CSVRecord record : records) {
                Map<String, String> rowMap = new LinkedHashMap<>();
                record.toMap().forEach(rowMap::put);
                rows.add(rowMap);
            }
        }
        log.info("Read {} rows from CSV: {}", rows.size(), file.getFileName());
        return rows;
    }

    private String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCachedFormulaResultType() == CellType.NUMERIC
                    ? String.valueOf((long) cell.getNumericCellValue())
                    : cell.getStringCellValue();
            default      -> "";
        };
    }

    // ── LLM helpers ───────────────────────────────────────────────────────────

    private String buildMatchPrompt(String userPrompt, String content, int maxResults) {
        return String.format("""
                You are a document-search assistant. Below is the content of a spreadsheet file.
                Each line is one row with fields as key=value pairs.

                SPREADSHEET DATA:
                %s

                USER QUERY: %s

                Task:
                1. Identify every row (by its row number) that is relevant to the user query.
                   Return at most %d rows.
                2. On the very FIRST line of your response output ONLY this format: ROWS: 1,3,7
                   If no rows match, output: ROWS: none
                3. After that line write a concise human-readable summary of the findings.
                """, content, userPrompt, maxResults);
    }

    private String buildOverallSummary(String prompt, List<FileSearchResult> results) {
        if (results.isEmpty()) return "No files were searched.";
        StringBuilder ctx = new StringBuilder();
        for (FileSearchResult r : results) {
            ctx.append("File: ").append(r.fileName())
               .append(" | Matches: ").append(r.matchedCount())
               .append("\n");
            if (r.matchedCount() > 0) {
                ctx.append("  Summary: ").append(r.summary()).append("\n");
            }
        }
        String overallPrompt = String.format("""
                A user searched multiple spreadsheet files with the query: "%s"
                Results per file:
                %s
                Write a concise overall summary (2-4 sentences) combining the findings.
                """, prompt, ctx);
        try {
            return chatModel.generate(overallPrompt).trim();
        } catch (Exception e) {
            log.error("Overall summary generation failed", e);
            return "Search complete. See individual file results below.";
        }
    }

    private List<Integer> parseRowNumbers(String llmReply, int totalRows) {
        for (String line : llmReply.split("\\n")) {
            String t = line.trim();
            if (t.toUpperCase().startsWith("ROWS:")) {
                String nums = t.substring("ROWS:".length()).trim();
                if (nums.equalsIgnoreCase("none") || nums.isBlank()) return Collections.emptyList();
                List<Integer> result = new ArrayList<>();
                for (String token : nums.split(",")) {
                    try {
                        int n = Integer.parseInt(token.trim());
                        if (n >= 1 && n <= totalRows) result.add(n);
                    } catch (NumberFormatException ignored) {}
                }
                return result;
            }
        }
        return Collections.emptyList();
    }

    private String extractSummary(String llmReply) {
        return Arrays.stream(llmReply.split("\\n"))
                .filter(line -> !line.trim().toUpperCase().startsWith("ROWS:"))
                .collect(Collectors.joining("\n"))
                .trim();
    }
}
