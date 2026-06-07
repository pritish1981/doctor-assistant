package com.superclinic.doctorassistant.ai.rag.dto;

import com.superclinic.doctorassistant.ai.rag.KnowledgeSourceType;

import java.util.List;

public record IngestionResult(
        KnowledgeSourceType sourceType,
        int documentsProcessed,
        int chunksIndexed,
        List<String> titles) {
}
