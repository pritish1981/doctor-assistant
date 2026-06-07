package com.superclinic.doctorassistant.ai.rag;

import com.superclinic.doctorassistant.ai.config.RagProperties;
import com.superclinic.doctorassistant.ai.rag.dto.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagRetrievalService {

    private final VectorStore vectorStore;
    private final RagProperties ragProperties;

    public List<Document> retrieve(String query) {
        return retrieve(query, Set.of(), ragProperties.getTopK());
    }

    public List<Document> retrieve(String query, Set<KnowledgeSourceType> sourceTypes, int topK) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        SearchRequest.Builder requestBuilder = SearchRequest.builder()
                .query(query.trim())
                .topK(topK)
                .similarityThreshold(ragProperties.getSimilarityThreshold());

        String filterExpression = buildFilterExpression(sourceTypes);
        if (StringUtils.hasText(filterExpression)) {
            requestBuilder.filterExpression(filterExpression);
        }

        List<Document> results = vectorStore.similaritySearch(requestBuilder.build());
        log.debug("RAG retrieval: query='{}', hits={}", abbreviate(query), results.size());
        return results;
    }

    public List<RetrievedChunk> retrieveChunks(String query, Set<KnowledgeSourceType> sourceTypes, int topK) {
        return retrieve(query, sourceTypes, topK).stream()
                .map(RetrievedChunk::from)
                .toList();
    }

    private String buildFilterExpression(Collection<KnowledgeSourceType> sourceTypes) {
        List<String> clauses = new ArrayList<>();
        if (StringUtils.hasText(ragProperties.getDefaultSourceFilter())) {
            clauses.add(ragProperties.getDefaultSourceFilter());
        }
        if (sourceTypes != null && !sourceTypes.isEmpty()) {
            String sourceTypeFilter = sourceTypes.stream()
                    .map(KnowledgeSourceType::name)
                    .map(name -> "'" + name + "'")
                    .collect(Collectors.joining(", "));
            clauses.add(RagMetadataKeys.SOURCE_TYPE + " IN [" + sourceTypeFilter + "]");
        }
        return clauses.isEmpty() ? null : String.join(" && ", clauses);
    }

    private String abbreviate(String query) {
        return query.length() <= 80 ? query : query.substring(0, 77) + "...";
    }
}
