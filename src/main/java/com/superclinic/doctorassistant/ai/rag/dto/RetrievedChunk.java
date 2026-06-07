package com.superclinic.doctorassistant.ai.rag.dto;

import com.superclinic.doctorassistant.ai.rag.RagMetadataKeys;
import org.springframework.ai.document.Document;

public record RetrievedChunk(
        String title,
        String sourceType,
        String sourceId,
        String category,
        int chunkIndex,
        double score,
        String content) {

    public static RetrievedChunk from(Document document) {
        var metadata = document.getMetadata();
        return new RetrievedChunk(
                stringValue(metadata.get(RagMetadataKeys.TITLE)),
                stringValue(metadata.get(RagMetadataKeys.SOURCE_TYPE)),
                stringValue(metadata.get(RagMetadataKeys.SOURCE_ID)),
                stringValue(metadata.get(RagMetadataKeys.CATEGORY)),
                intValue(metadata.get(RagMetadataKeys.CHUNK_INDEX)),
                document.getScore() != null ? document.getScore() : 0.0,
                document.getText());
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text);
        }
        return 0;
    }
}
