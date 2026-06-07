package com.superclinic.doctorassistant.ai.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RagVectorStoreSupport {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    public void deleteBySource(KnowledgeSourceType sourceType, UUID sourceId, String title) {
        String filter = buildDeleteFilter(sourceType, sourceId, title);
        try {
            vectorStore.delete(filter);
        } catch (Exception ex) {
            deleteBySourceUsingJdbc(sourceType, sourceId, title);
        }
    }

    private void deleteBySourceUsingJdbc(KnowledgeSourceType sourceType, UUID sourceId, String title) {
        if (sourceId != null) {
            jdbcTemplate.update("""
                    DELETE FROM vector_store
                    WHERE metadata ->> 'source_type' = ?
                      AND metadata ->> 'source_id' = ?
                    """, sourceType.name(), sourceId.toString());
            return;
        }
        if (StringUtils.hasText(title)) {
            jdbcTemplate.update("""
                    DELETE FROM vector_store
                    WHERE metadata ->> 'source_type' = ?
                      AND metadata ->> 'title' = ?
                    """, sourceType.name(), title);
        }
    }

    private String buildDeleteFilter(KnowledgeSourceType sourceType, UUID sourceId, String title) {
        if (sourceId != null) {
            return "%s == '%s' && %s == '%s'".formatted(
                    RagMetadataKeys.SOURCE_TYPE, sourceType.name(),
                    RagMetadataKeys.SOURCE_ID, sourceId);
        }
        return "%s == '%s' && %s == '%s'".formatted(
                RagMetadataKeys.SOURCE_TYPE, sourceType.name(),
                RagMetadataKeys.TITLE, escapeFilterValue(title));
    }

    private String escapeFilterValue(String value) {
        return value.replace("'", "\\'");
    }
}
