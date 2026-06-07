package com.superclinic.doctorassistant.persistence.repository;

import com.superclinic.doctorassistant.persistence.entity.KnowledgeDocument;
import com.superclinic.doctorassistant.persistence.entity.enums.KnowledgeDocumentSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID> {

    List<KnowledgeDocument> findBySourceTypeAndPublishedTrueOrderByTitleAscChunkIndexAsc(
            KnowledgeDocumentSourceType sourceType);

    List<KnowledgeDocument> findBySourceTypeAndSourceIdOrderByChunkIndexAsc(
            KnowledgeDocumentSourceType sourceType, UUID sourceId);

    @Modifying
    @Query("""
            DELETE FROM KnowledgeDocument kd
            WHERE kd.sourceType = :sourceType
              AND kd.sourceId = :sourceId
            """)
    void deleteBySourceTypeAndSourceId(
            @Param("sourceType") KnowledgeDocumentSourceType sourceType,
            @Param("sourceId") UUID sourceId);

    @Modifying
    @Query("""
            DELETE FROM KnowledgeDocument kd
            WHERE kd.sourceType = :sourceType
              AND kd.sourceId IS NULL
              AND kd.title = :title
            """)
    void deleteBySourceTypeAndTitleWithoutSourceId(
            @Param("sourceType") KnowledgeDocumentSourceType sourceType,
            @Param("title") String title);
}
