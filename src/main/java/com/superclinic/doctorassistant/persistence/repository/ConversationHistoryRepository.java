package com.superclinic.doctorassistant.persistence.repository;

import com.superclinic.doctorassistant.persistence.entity.ConversationHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationHistoryRepository extends JpaRepository<ConversationHistoryEntry, UUID> {

    List<ConversationHistoryEntry> findBySessionIdOrderBySequenceNumberAsc(UUID sessionId);

    @Query("""
            SELECT COALESCE(MAX(h.sequenceNumber), 0)
            FROM ConversationHistoryEntry h
            WHERE h.session.id = :sessionId
            """)
    int findMaxSequenceNumber(@Param("sessionId") UUID sessionId);

    long countBySessionId(UUID sessionId);
}
