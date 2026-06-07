package com.superclinic.doctorassistant.persistence.repository;

import com.superclinic.doctorassistant.ai.rag.KnowledgeSourceType;
import com.superclinic.doctorassistant.persistence.entity.RagSourceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RagSourceCatalogRepository extends JpaRepository<RagSourceCatalog, UUID> {

    Optional<RagSourceCatalog> findBySourceTypeAndSourceIdAndTitle(
            KnowledgeSourceType sourceType, UUID sourceId, String title);

    List<RagSourceCatalog> findBySourceTypeOrderByTitleAsc(KnowledgeSourceType sourceType);
}
