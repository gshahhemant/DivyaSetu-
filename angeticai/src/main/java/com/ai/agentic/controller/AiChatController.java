package com.ai.agentic.controller;

import com.ai.agentic.dto.ChatRequest;
import com.ai.agentic.dto.ChatResponse;
import com.ai.agentic.service.AiChatService;
import com.ai.agentic.service.DatabaseSchemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "AI Chat", description = "Ask natural-language questions. The AI auto-generates SQL, queries your PostgreSQL DB, and returns a human-readable answer.")
public class AiChatController {

    private final AiChatService       aiChatService;
    private final DatabaseSchemaService schemaService;

    public AiChatController(AiChatService aiChatService,
                             DatabaseSchemaService schemaService) {
        this.aiChatService  = aiChatService;
        this.schemaService  = schemaService;
    }

    @PostMapping
    @Operation(
        summary = "Ask a question (Text-to-SQL)",
        description = """
            Send a natural-language question. The service will:
            1. Read your live PostgreSQL schema.
            2. Ask Ollama (gemma3:12b) to generate the right SQL SELECT.
            3. Execute the SQL safely.
            4. Ask Ollama to summarise the results in plain English.
            """
    )
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.prompt() == null || request.prompt().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Prompt must not be empty.", "", 0));
        }
        return ResponseEntity.ok(aiChatService.chat(request.prompt()));
    }

    @GetMapping("/ask")
    @Operation(
        summary = "Quick ask via query param",
        description = "Convenience GET endpoint — pass your question as ?prompt="
    )
    public ResponseEntity<ChatResponse> quickAsk(@RequestParam String prompt) {
        if (prompt.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Prompt must not be empty.", "", 0));
        }
        return ResponseEntity.ok(aiChatService.chat(prompt));
    }

    @GetMapping("/schema")
    @Operation(
        summary = "View detected DB schema",
        description = "Returns the live table/column schema that the AI uses to generate SQL."
    )
    public ResponseEntity<Map<String, Object>> schema() {
        List<String> tables = schemaService.getTableNames();
        String description  = schemaService.getSchemaDescription();
        return ResponseEntity.ok(Map.of(
                "tables", tables,
                "schema", description
        ));
    }
}
