package com.ai.agentic.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Executes AI-generated SQL safely.
 * Only SELECT statements are allowed — any other DML/DDL is rejected.
 */
@Service
public class QueryExecutorService {

    private static final int MAX_ROWS = 100;

    // Keywords that must never appear in an AI-generated query
    private static final List<String> BLOCKED_KEYWORDS = List.of(
            "INSERT", "UPDATE", "DELETE", "DROP", "ALTER",
            "TRUNCATE", "CREATE", "EXECUTE", "EXEC", "GRANT",
            "REVOKE", "CALL", "MERGE", "REPLACE", "COPY"
    );

    private final JdbcTemplate jdbcTemplate;

    public QueryExecutorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Validates that {@code sql} is a safe SELECT query, then executes it
     * and returns up to {@value MAX_ROWS} rows as a list of column→value maps.
     *
     * @throws IllegalArgumentException if the query fails security validation
     */
    public List<Map<String, Object>> executeSelect(String sql) {
        String sanitized = sanitize(sql);
        validate(sanitized);
        // Always enforce a row limit for safety
        String limited = applyLimit(sanitized);
        return jdbcTemplate.queryForList(limited);
    }

    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    /** Strip markdown code fences the model may have added. */
    private String sanitize(String sql) {
        return sql.replaceAll("(?i)```sql", "")
                  .replaceAll("```", "")
                  .trim();
    }

    private void validate(String sql) {
        String upper = sql.toUpperCase();

        if (!upper.startsWith("SELECT")) {
            throw new IllegalArgumentException(
                    "Security violation: only SELECT queries are permitted. Got: " +
                    sql.substring(0, Math.min(sql.length(), 60)));
        }

        for (String kw : BLOCKED_KEYWORDS) {
            // Match whole word to avoid false positives (e.g. "selection")
            if (upper.matches("(?s).*\\b" + kw + "\\b.*")) {
                throw new IllegalArgumentException(
                        "Security violation: query contains blocked keyword '" + kw + "'");
            }
        }
    }

    /**
     * Appends LIMIT if not already present, so large tables don't flood the response.
     */
    private String applyLimit(String sql) {
        if (!sql.toUpperCase().contains("LIMIT")) {
            // Remove trailing semicolon before appending LIMIT
            sql = sql.replaceAll(";\\s*$", "");
            return sql + " LIMIT " + MAX_ROWS;
        }
        return sql;
    }
}
