package com.superclinic.doctorassistant.ai.rag;

import java.util.Map;
import java.util.UUID;

public record IngestibleDocument(
        KnowledgeSourceType sourceType,
        UUID sourceId,
        String title,
        String content,
        boolean published,
        Map<String, Object> metadata) {

    public IngestibleDocument {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
