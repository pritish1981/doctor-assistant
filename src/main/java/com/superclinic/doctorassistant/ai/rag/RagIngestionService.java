package com.superclinic.doctorassistant.ai.rag;

import com.superclinic.doctorassistant.ai.config.RagProperties;
import com.superclinic.doctorassistant.ai.rag.dto.IngestionResult;
import com.superclinic.doctorassistant.persistence.entity.RagSourceCatalog;
import com.superclinic.doctorassistant.persistence.repository.DoctorRepository;
import com.superclinic.doctorassistant.persistence.repository.RagSourceCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagIngestionService {

    private final VectorStore vectorStore;
    private final RagDocumentChunker documentChunker;
    private final DoctorRepository doctorRepository;
    private final DoctorProfileDocumentBuilder doctorProfileDocumentBuilder;
    private final KnowledgeDocumentLoader knowledgeDocumentLoader;
    private final ClasspathPdfKnowledgeLoader classpathPdfKnowledgeLoader;
    private final RagProperties ragProperties;
    private final RagSourceCatalogRepository ragSourceCatalogRepository;
    private final RagVectorStoreSupport vectorStoreSupport;

    @Transactional
    public List<IngestionResult> ingestAll() {
        List<IngestionResult> results = new ArrayList<>();
        for (KnowledgeSourceType sourceType : KnowledgeSourceType.values()) {
            results.add(ingestSourceType(sourceType));
        }
        log.info("RAG full ingestion completed: {} source type(s)", results.size());
        return results;
    }

    @Transactional
    public IngestionResult ingestSourceType(KnowledgeSourceType sourceType) {
        if (ragProperties.getClasspathDocs().isEnabled()) {
            List<IngestibleDocument> pdfDocuments = classpathPdfKnowledgeLoader.loadBySourceType(sourceType);
            if (!pdfDocuments.isEmpty()) {
                return ingestDocuments(sourceType, pdfDocuments);
            }
        }

        return switch (sourceType) {
            case DOCTOR_PROFILE -> ingestDoctorProfiles();
            case FAQ, INSURANCE_POLICY, APPOINTMENT_POLICY -> ingestKnowledgeDocuments(sourceType);
        };
    }

    @Transactional
    public IngestionResult ingestDoctorProfiles() {
        var doctors = doctorRepository.findAllActiveWithSpecialty();
        int totalChunks = 0;
        List<String> titles = new ArrayList<>();

        for (var doctor : doctors) {
            IngestibleDocument source = doctorProfileDocumentBuilder.toIngestibleDocument(doctor);
            int chunks = ingestDocument(source);
            totalChunks += chunks;
            titles.add(source.title());
        }

        log.info("Ingested {} doctor profile(s), {} chunk(s)", doctors.size(), totalChunks);
        return new IngestionResult(KnowledgeSourceType.DOCTOR_PROFILE, doctors.size(), totalChunks, titles);
    }

    @Transactional
    public IngestionResult ingestKnowledgeDocuments(KnowledgeSourceType sourceType) {
        List<IngestibleDocument> sources = knowledgeDocumentLoader.loadPublishedBySourceType(sourceType);
        return ingestDocuments(sourceType, sources);
    }

    private IngestionResult ingestDocuments(KnowledgeSourceType sourceType, List<IngestibleDocument> sources) {
        int totalChunks = 0;
        List<String> titles = new ArrayList<>();

        for (IngestibleDocument source : sources) {
            int chunks = ingestDocument(source);
            totalChunks += chunks;
            titles.add(source.title());
        }

        log.info("Ingested {} {} document(s), {} chunk(s)", sources.size(), sourceType, totalChunks);
        return new IngestionResult(sourceType, sources.size(), totalChunks, titles);
    }

    @Transactional
    public int ingestDocument(IngestibleDocument source) {
        if (!StringUtils.hasText(source.content())) {
            log.warn("Skipping ingestion for empty document: {}", source.title());
            return 0;
        }

        vectorStoreSupport.deleteBySource(source.sourceType(), source.sourceId(), source.title());

        List<Document> chunks = documentChunker.chunk(source);
        if (chunks.isEmpty()) {
            return 0;
        }

        vectorStore.add(chunks);
        upsertCatalogEntry(source, chunks.size());

        log.debug("Ingested '{}' into vector store ({} chunk(s))", source.title(), chunks.size());
        return chunks.size();
    }

    private void upsertCatalogEntry(IngestibleDocument source, int chunkCount) {
        RagSourceCatalog catalog = ragSourceCatalogRepository
                .findBySourceTypeAndSourceIdAndTitle(source.sourceType(), source.sourceId(), source.title())
                .orElseGet(() -> {
                    RagSourceCatalog entry = new RagSourceCatalog();
                    entry.setCreatedAt(Instant.now());
                    return entry;
                });

        catalog.setSourceType(source.sourceType());
        catalog.setSourceId(source.sourceId());
        catalog.setTitle(source.title());
        catalog.setChunkCount(chunkCount);
        catalog.setPublished(source.published());
        catalog.setLastIngestedAt(Instant.now());
        catalog.setUpdatedAt(Instant.now());
        if (catalog.getCreatedAt() == null) {
            catalog.setCreatedAt(Instant.now());
        }

        ragSourceCatalogRepository.save(catalog);
    }
}
