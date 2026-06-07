package com.superclinic.doctorassistant.ai.rag;

import com.superclinic.doctorassistant.ai.config.RagProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class RagDocumentChunker {

    private final TokenTextSplitter faqTextSplitter;
    private final TokenTextSplitter insurancePolicyTextSplitter;
    private final TokenTextSplitter doctorProfileTextSplitter;

    public RagDocumentChunker(
            @Qualifier("faqTextSplitter") TokenTextSplitter faqTextSplitter,
            @Qualifier("insurancePolicyTextSplitter") TokenTextSplitter insurancePolicyTextSplitter,
            @Qualifier("doctorProfileTextSplitter") TokenTextSplitter doctorProfileTextSplitter) {
        this.faqTextSplitter = faqTextSplitter;
        this.insurancePolicyTextSplitter = insurancePolicyTextSplitter;
        this.doctorProfileTextSplitter = doctorProfileTextSplitter;
    }

    public List<Document> chunk(IngestibleDocument source) {
        if (!StringUtils.hasText(source.content())) {
            log.warn("Skipping empty document: sourceType={}, title={}", source.sourceType(), source.title());
            return List.of();
        }

        Document parent = Document.builder()
                .text(source.content())
                .metadata(buildBaseMetadata(source))
                .build();

        List<Document> chunks = selectSplitter(source.sourceType()).split(parent);
        return assignChunkIndexes(chunks);
    }

    private TokenTextSplitter selectSplitter(KnowledgeSourceType sourceType) {
        return switch (sourceType) {
            case FAQ -> faqTextSplitter;
            case INSURANCE_POLICY, APPOINTMENT_POLICY -> insurancePolicyTextSplitter;
            case DOCTOR_PROFILE -> doctorProfileTextSplitter;
        };
    }

    private Map<String, Object> buildBaseMetadata(IngestibleDocument source) {
        Map<String, Object> metadata = new HashMap<>(source.metadata());
        metadata.put(RagMetadataKeys.SOURCE_TYPE, source.sourceType().name());
        if (source.sourceId() != null) {
            metadata.put(RagMetadataKeys.SOURCE_ID, source.sourceId().toString());
        }
        metadata.put(RagMetadataKeys.TITLE, source.title());
        metadata.put(RagMetadataKeys.PUBLISHED, source.published());
        if (!metadata.containsKey(RagMetadataKeys.CATEGORY)) {
            metadata.put(RagMetadataKeys.CATEGORY, defaultCategory(source.sourceType()));
        }
        return metadata;
    }

    private String defaultCategory(KnowledgeSourceType sourceType) {
        return switch (sourceType) {
            case DOCTOR_PROFILE -> "doctor";
            case FAQ -> "faq";
            case INSURANCE_POLICY -> "insurance";
            case APPOINTMENT_POLICY -> "appointments";
        };
    }

    private List<Document> assignChunkIndexes(List<Document> chunks) {
        List<Document> indexed = new ArrayList<>(chunks.size());
        for (int index = 0; index < chunks.size(); index++) {
            Document chunk = chunks.get(index);
            Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
            metadata.put(RagMetadataKeys.CHUNK_INDEX, index);
            indexed.add(Document.builder()
                    .id(buildChunkId(metadata))
                    .text(chunk.getText())
                    .metadata(metadata)
                    .build());
        }
        return indexed;
    }

    private String buildChunkId(Map<String, Object> metadata) {
        String sourceType = metadata.get(RagMetadataKeys.SOURCE_TYPE).toString();
        Object sourceId = metadata.get(RagMetadataKeys.SOURCE_ID);
        Object title = metadata.get(RagMetadataKeys.TITLE);
        Object chunkIndex = metadata.get(RagMetadataKeys.CHUNK_INDEX);
        String identity = sourceId != null ? sourceId.toString() : title.toString();
        return UUID.nameUUIDFromBytes(
                "%s:%s:%s".formatted(sourceType, identity, chunkIndex).getBytes())
                .toString();
    }
}
