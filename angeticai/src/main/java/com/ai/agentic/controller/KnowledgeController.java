package com.ai.agentic.controller;

import com.ai.agentic.entity.KnowledgeEntry;
import com.ai.agentic.repository.KnowledgeEntryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@Tag(name = "Knowledge Base", description = "CRUD operations for the PostgreSQL knowledge base that the AI uses as context")
public class KnowledgeController {

    private final KnowledgeEntryRepository repository;

    public KnowledgeController(KnowledgeEntryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "List all knowledge entries")
    public List<KnowledgeEntry> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single knowledge entry by ID")
    public ResponseEntity<KnowledgeEntry> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search entries by keyword in title or content")
    public List<KnowledgeEntry> search(@RequestParam String keyword) {
        return repository.searchByKeyword(keyword);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Find entries by category")
    public List<KnowledgeEntry> findByCategory(@PathVariable String category) {
        return repository.findByCategoryIgnoreCase(category);
    }

    @PostMapping
    @Operation(
        summary = "Add a new knowledge entry",
        description = "Adds a new entry to the database. The AI will use this data when answering questions."
    )
    public ResponseEntity<KnowledgeEntry> create(@RequestBody KnowledgeEntry entry) {
      //  entry.setId(null); // ensure it's a new insert
        KnowledgeEntry saved = repository.save(entry);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing knowledge entry")
    public ResponseEntity<KnowledgeEntry> update(@PathVariable Long id,
                                                  @RequestBody KnowledgeEntry entry) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
     //   entry.setId(id);
        return ResponseEntity.ok(repository.save(entry));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a knowledge entry")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
