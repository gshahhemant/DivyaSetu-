package com.ai.agentic.repository;

import com.ai.agentic.entity.KnowledgeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeEntryRepository extends JpaRepository<KnowledgeEntry, Long> {

    @Query("SELECT k FROM KnowledgeEntry k WHERE " +
           "LOWER(k.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<KnowledgeEntry> searchByKeyword(@Param("keyword") String keyword);

    List<KnowledgeEntry> findByCategory(String category);

    List<KnowledgeEntry> findByCategoryIgnoreCase(String category);
}
