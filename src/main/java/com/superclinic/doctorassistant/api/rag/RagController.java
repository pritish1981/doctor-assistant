package com.superclinic.doctorassistant.api.rag;

import com.superclinic.doctorassistant.ai.rag.KnowledgeSourceType;
import com.superclinic.doctorassistant.ai.rag.RagService;
import com.superclinic.doctorassistant.ai.rag.dto.IngestionResult;
import com.superclinic.doctorassistant.ai.rag.dto.RetrievedChunk;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
@Tag(name = "RAG", description = "Knowledge base ingestion and retrieval")
public class RagController {

    private final RagService ragService;

    @PostMapping("/ingest")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Ingest all knowledge sources into the vector store")
    public List<IngestionResult> ingestAll() {
        return ragService.ingestAll();
    }

    @PostMapping("/ingest/{sourceType}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Ingest a single knowledge source type")
    public IngestionResult ingestSourceType(@PathVariable KnowledgeSourceType sourceType) {
        return ragService.ingestSourceType(sourceType);
    }

    @GetMapping("/search")
    @Operation(summary = "Semantic search over the clinic knowledge base")
    public List<RetrievedChunk> search(
            @RequestParam String query,
            @RequestParam(required = false) List<KnowledgeSourceType> sourceTypes,
            @RequestParam(required = false) Integer topK) {
        Set<KnowledgeSourceType> filter = sourceTypes == null || sourceTypes.isEmpty()
                ? Set.of()
                : Set.copyOf(sourceTypes);
        return ragService.search(query, filter, topK);
    }
}
