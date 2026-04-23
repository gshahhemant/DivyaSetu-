package com.ai.agentic.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Introspects the PostgreSQL public schema at runtime so the AI always has
 * an up-to-date picture of every table and column it can query.
 */
@Service
public class DatabaseSchemaService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns a human-readable schema description, e.g.:
     * <pre>
     * Table: contacts
     *   - id (integer)
     *   - first_name (character varying)
     *   ...
     * </pre>
     */
    public String getSchemaDescription() {
        String sql = """
                SELECT table_name, column_name, data_type
                FROM information_schema.columns
                WHERE table_schema = 'public'
                ORDER BY table_name, ordinal_position
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        // Group columns by table
        Map<String, StringBuilder> tables = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String table  = (String) row.get("table_name");
            String column = (String) row.get("column_name");
            String type   = (String) row.get("data_type");
            tables.computeIfAbsent(table, t -> new StringBuilder())
                  .append("  - ").append(column).append(" (").append(type).append(")\n");
        }

        if (tables.isEmpty()) {
            return "No tables found in the public schema.";
        }

        StringBuilder schema = new StringBuilder();
        tables.forEach((table, cols) -> {
            schema.append("Table: ").append(table).append("\n");
            schema.append(cols);
            schema.append("\n");
        });
        return schema.toString().trim();
    }

    /** Returns just the list of table names. */
    public List<String> getTableNames() {
        return jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema = 'public' ORDER BY table_name",
                String.class);
    }
}
