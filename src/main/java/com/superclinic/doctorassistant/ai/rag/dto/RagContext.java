package com.superclinic.doctorassistant.ai.rag.dto;

import org.springframework.ai.document.Document;

import java.util.List;

public record RagContext(
        String query,
        String formattedContext,
        List<Document> retrievedDocuments) {

    public boolean hasContext() {
        return !retrievedDocuments.isEmpty();
    }
}
