package com.ai.agentic.service;

import com.ai.agentic.dto.ChatResponse;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Text-to-SQL RAG service backed by OpenAI (online).
 * Flow:
 *   1. Introspect the live DB schema.
 *   2. Ask OpenAI to generate a SQL SELECT for the user's prompt.
 *   3. Execute that SQL safely (SELECT-only, row-limited).
 *   4. Ask OpenAI to convert the raw rows into a natural-language answer.
 */
@Service
public class OnlineAiChatService {

    private static final Logger log = LoggerFactory.getLogger(OnlineAiChatService.class);

    private final DatabaseSchemaService schemaService;
    private final QueryExecutorService  queryExecutor;
    private final ChatLanguageModel     chatModel;

    public OnlineAiChatService(
            DatabaseSchemaService schemaService,
            QueryExecutorService  queryExecutor,
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}")   String modelName,
            @Value("${openai.timeout}") int timeoutSeconds) {

        this.schemaService = schemaService;
        this.queryExecutor = queryExecutor;
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    public ChatResponse chat(String userPrompt) {

        // ── Step 1: get live schema ───────────────────────────────────────────
        String schema = schemaService.getSchemaDescription();

        // ── Step 2: generate SQL ──────────────────────────────────────────────
        String sqlPrompt = buildSqlGenerationPrompt(schema, userPrompt);
        String generatedSql = chatModel.generate(sqlPrompt).trim();
        log.info("Generated SQL: {}", generatedSql);

        // ── Step 3: execute SQL safely ────────────────────────────────────────
        List<Map<String, Object>> rows;
        try {
            rows = queryExecutor.executeSelect(generatedSql);
        } catch (IllegalArgumentException e) {
            return new ChatResponse(
                    "Could not execute the query: " + e.getMessage(),
                    generatedSql,
                    0);
        } catch (Exception e) {
            log.error("SQL execution failed", e);
            return new ChatResponse(
                    "Query execution error: " + e.getMessage(),
                    generatedSql,
                    0);
        }

        // ── Step 4: natural-language answer ──────────────────────────────────
        String answerPrompt = buildAnswerPrompt(userPrompt, generatedSql, rows);
        String answer = chatModel.generate(answerPrompt);

        return new ChatResponse(answer, generatedSql, rows.size());
    }

    // ── prompt builders ───────────────────────────────────────────────────────

    private String buildSqlGenerationPrompt(String schema, String userPrompt) {
        return """
                You are a PostgreSQL expert. Given the database schema below, write a single valid SQL SELECT query that answers the user's question.
                Rules:
                - Return ONLY the raw SQL query — no explanation, no markdown, no code fences.
                - Use only SELECT statements. Never use INSERT, UPDATE, DELETE, DROP, or any DDL.
                - If the question cannot be answered from the schema, return: SELECT 'No relevant data found' AS message;

                === DATABASE SCHEMA ===
                %s
                === END OF SCHEMA ===

                User question: %s

                SQL:""".formatted(schema, userPrompt.trim());
    }

    private String buildAnswerPrompt(String userPrompt,
                                     String sql,
                                     List<Map<String, Object>> rows) {
        String dataStr = rows.isEmpty()
                ? "(no rows returned)"
                : formatRows(rows);

        return """
                You are a helpful data assistant. The user asked a question and the following SQL query was executed against their database.
                Summarise the results in clear, friendly, natural language. Do not repeat the SQL.

                User question: %s

                SQL executed:
                %s

                Query results (%d row(s)):
                %s

                Answer:""".formatted(userPrompt.trim(), sql, rows.size(), dataStr);
    }

    private String formatRows(List<Map<String, Object>> rows) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> first = rows.get(0);
        sb.append(String.join(" | ", first.keySet())).append("\n");
        sb.append("-".repeat(60)).append("\n");
        for (Map<String, Object> row : rows) {
            sb.append(String.join(" | ",
                    row.values().stream()
                       .map(v -> v == null ? "null" : v.toString())
                       .toList()))
              .append("\n");
        }
        return sb.toString();
    }
}
