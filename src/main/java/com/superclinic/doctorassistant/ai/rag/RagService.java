package com.superclinic.doctorassistant.ai.rag;

import com.superclinic.doctorassistant.ai.config.RagProperties;
import com.superclinic.doctorassistant.ai.rag.dto.IngestionResult;
import com.superclinic.doctorassistant.ai.rag.dto.RagContext;
import com.superclinic.doctorassistant.ai.rag.dto.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {

    private static final String CONTEXT_HEADER = """
            ## Retrieved clinic knowledge (use for FAQ, appointment policies, insurance, and doctor profiles)
            The following excerpts were retrieved from the clinic knowledge base.
            Prefer live tool results for availability, booking, and current doctor search.
            If the excerpts do not answer the question, say so and use tools or ask for clarification.

            """;

    private final RagIngestionService ingestionService;
    private final RagRetrievalService retrievalService;
    private final RagProperties ragProperties;

    public RagContext retrieveContext(String query) {
        return retrieveContext(query, Set.of());
    }

    public RagContext retrieveContext(String query, Set<KnowledgeSourceType> sourceTypes) {
        List<Document> documents = retrievalService.retrieve(query, sourceTypes, ragProperties.getTopK());
        String formattedContext = formatContext(documents);
        return new RagContext(query, formattedContext, documents);
    }

    public List<RetrievedChunk> search(String query, Set<KnowledgeSourceType> sourceTypes, Integer topK) {
        int limit = topK != null ? topK : ragProperties.getTopK();
        return retrievalService.retrieveChunks(query, sourceTypes, limit);
    }

    public List<IngestionResult> ingestAll() {
        return ingestionService.ingestAll();
    }

    public IngestionResult ingestSourceType(KnowledgeSourceType sourceType) {
        return ingestionService.ingestSourceType(sourceType);
    }

    public String augmentUserMessage(String userMessage, RagContext context) {
        if (context == null || !context.hasContext() || !StringUtils.hasText(context.formattedContext())) {
            return userMessage;
        }
        return CONTEXT_HEADER + context.formattedContext() + "\n\n## Patient message\n" + userMessage;
    }

    public String formatContext(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "";
        }
        return documents.stream()
                .map(this::formatDocument)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private String formatDocument(Document document) {
        var metadata = document.getMetadata();
        String title = stringValue(metadata.get(RagMetadataKeys.TITLE), "Untitled");
        String sourceType = stringValue(metadata.get(RagMetadataKeys.SOURCE_TYPE), "UNKNOWN");
        String category = stringValue(metadata.get(RagMetadataKeys.CATEGORY), "general");
        Double score = document.getScore();

        StringBuilder formatted = new StringBuilder();
        formatted.append("Title: ").append(title).append('\n');
        formatted.append("Source: ").append(sourceType).append(" / ").append(category).append('\n');
        if (score != null) {
            formatted.append("Relevance: ").append(String.format("%.3f", score)).append('\n');
        }
        formatted.append("Content:\n").append(document.getText());
        return formatted.toString();
    }

    private String stringValue(Object value, String defaultValue) {
        return value == null ? defaultValue : value.toString();
    }
}
