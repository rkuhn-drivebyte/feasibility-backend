package de.numcodex.feasibility_gui_backend.repository;

import de.numcodex.feasibility_gui_backend.model.db.QueryContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QueryContentRepository extends JpaRepository<QueryContent, Long> {
    @Query("SELECT t FROM QueryContent t WHERE t.hash = ?1")
    Optional<QueryContent> findByHash(String queryContentHash);
}