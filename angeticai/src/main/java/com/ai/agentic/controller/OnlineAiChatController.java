package com.ai.agentic.controller;

import com.ai.agentic.dto.ChatRequest;
import com.ai.agentic.dto.ChatResponse;
import com.ai.agentic.service.DatabaseSchemaService;
import com.ai.agentic.service.OnlineAiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/online-chat")
@Tag(name = "Online AI Chat", description = "Ask natural-language questions powered by OpenAI. The AI auto-generates SQL, queries your PostgreSQL DB, and returns a human-readable answer.")
public class OnlineAiChatController {

    private final OnlineAiChatService   onlineAiChatService;
    private final DatabaseSchemaService schemaService;

    public OnlineAiChatController(OnlineAiChatService onlineAiChatService,
                                   DatabaseSchemaService schemaService) {
        this.onlineAiChatService = onlineAiChatService;
        this.schemaService       = schemaService;
    }

    @PostMapping
    @Operation(
        summary = "Ask a question (Text-to-SQL via OpenAI)",
        description = """
            Send a natural-language question. The service will:
            1. Read your live PostgreSQL schema.
            2. Ask OpenAI (gpt-4o) to generate the right SQL SELECT.
            3. Execute the SQL safely.
            4. Ask OpenAI to summarise the results in plain English.
            """
    )
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.prompt() == null || request.prompt().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Prompt must not be empty.", "", 0));
        }
        return ResponseEntity.ok(onlineAiChatService.chat(request.prompt()));
    }

    @GetMapping("/ask")
    @Operation(
        summary = "Quick ask via query param (OpenAI)",
        description = "Convenience GET endpoint — pass your question as ?prompt="
    )
    public ResponseEntity<ChatResponse> quickAsk(@RequestParam String prompt) {
        if (prompt.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Prompt must not be empty.", "", 0));
        }
        return ResponseEntity.ok(onlineAiChatService.chat(prompt));
    }

    @GetMapping("/schema")
    @Operation(
        summary = "View detected DB schema",
        description = "Returns the live table/column schema that the AI uses to generate SQL."
    )
    public ResponseEntity<Map<String, Object>> schema() {
        List<String> tables   = schemaService.getTableNames();
        String description    = schemaService.getSchemaDescription();
        return ResponseEntity.ok(Map.of(
                "tables", tables,
                "schema", description
        ));
    }
}
