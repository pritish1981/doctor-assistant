package com.superclinic.doctorassistant.ai.rag;

import com.superclinic.doctorassistant.persistence.entity.KnowledgeDocument;
import com.superclinic.doctorassistant.persistence.entity.enums.KnowledgeDocumentSourceType;
import com.superclinic.doctorassistant.persistence.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KnowledgeDocumentLoader {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    public List<IngestibleDocument> loadPublishedBySourceType(KnowledgeSourceType sourceType) {
        KnowledgeDocumentSourceType documentSourceType = toDocumentSourceType(sourceType);
        return knowledgeDocumentRepository
                .findBySourceTypeAndPublishedTrueOrderByTitleAscChunkIndexAsc(documentSourceType)
                .stream()
                .map(this::toIngestibleDocument)
                .toList();
    }

    private IngestibleDocument toIngestibleDocument(KnowledgeDocument document) {
        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        if (!metadata.containsKey(RagMetadataKeys.CATEGORY)) {
            metadata.put(RagMetadataKeys.CATEGORY, defaultCategory(document.getSourceType()));
        }
        return new IngestibleDocument(
                toKnowledgeSourceType(document.getSourceType()),
                document.getSourceId(),
                document.getTitle(),
                document.getContent(),
                document.getPublished(),
                metadata);
    }

    private KnowledgeSourceType toKnowledgeSourceType(KnowledgeDocumentSourceType sourceType) {
        return switch (sourceType) {
            case DOCTOR_PROFILE -> KnowledgeSourceType.DOCTOR_PROFILE;
            case FAQ -> KnowledgeSourceType.FAQ;
            case INSURANCE_POLICY -> KnowledgeSourceType.INSURANCE_POLICY;
            case CLINIC_POLICY -> KnowledgeSourceType.APPOINTMENT_POLICY;
            case TRIAGE_GUIDE -> throw new IllegalArgumentException(
                    "Source type not supported for RAG ingestion: " + sourceType);
        };
    }

    private KnowledgeDocumentSourceType toDocumentSourceType(KnowledgeSourceType sourceType) {
        return switch (sourceType) {
            case DOCTOR_PROFILE -> KnowledgeDocumentSourceType.DOCTOR_PROFILE;
            case FAQ -> KnowledgeDocumentSourceType.FAQ;
            case INSURANCE_POLICY -> KnowledgeDocumentSourceType.INSURANCE_POLICY;
            case APPOINTMENT_POLICY -> KnowledgeDocumentSourceType.CLINIC_POLICY;
        };
    }

    private String defaultCategory(KnowledgeDocumentSourceType sourceType) {
        return switch (sourceType) {
            case DOCTOR_PROFILE -> "doctor";
            case FAQ -> "faq";
            case INSURANCE_POLICY -> "insurance";
            case TRIAGE_GUIDE -> "triage";
            case CLINIC_POLICY -> "policy";
        };
    }
}
