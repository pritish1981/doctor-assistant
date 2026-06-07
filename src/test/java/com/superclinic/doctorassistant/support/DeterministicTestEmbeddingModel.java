package com.superclinic.doctorassistant.support;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic embeddings for integration tests — similar texts share vector buckets
 * so pgvector similarity search is repeatable without calling OpenAI.
 */
public class DeterministicTestEmbeddingModel implements EmbeddingModel {

    private final int dimensions;

    public DeterministicTestEmbeddingModel(int dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = request.getInstructions().stream()
                .map(text -> new Embedding(toVector(text), 0))
                .toList();
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return toVector(document.getText());
    }

    @Override
    public float[] embed(String text) {
        return toVector(text);
    }

    private float[] toVector(String text) {
        float[] vector = new float[dimensions];
        String normalized = text.toLowerCase(Locale.ROOT);

        applyBucket(vector, 0, containsAny(normalized, "faq", "book an appointment", "how do i book", "operating hours", "clinic hours"));
        applyBucket(vector, 1, containsAny(normalized, "sharma", "cardio", "cardiologist", "heart", "hypertension"));
        applyBucket(vector, 2, containsAny(normalized, "nair", "dermat", "skin", "eczema"));
        applyBucket(vector, 3, containsAny(normalized, "insurance", "copay", "coverage"));

        if (isZeroVector(vector)) {
            int hash = normalized.hashCode();
            for (int i = 0; i < Math.min(dimensions, 32); i++) {
                vector[i] = (float) Math.sin(hash + i);
            }
        }

        normalize(vector);
        return vector;
    }

    private static boolean containsAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }

    private static void applyBucket(float[] vector, int bucketIndex, boolean active) {
        if (active && bucketIndex < vector.length) {
            vector[bucketIndex] = 1.0f;
        }
    }

    private static boolean isZeroVector(float[] vector) {
        for (float value : vector) {
            if (value != 0.0f) {
                return false;
            }
        }
        return true;
    }

    private static void normalize(float[] vector) {
        double sumSquares = 0.0;
        for (float value : vector) {
            sumSquares += value * value;
        }
        if (sumSquares == 0.0) {
            return;
        }
        double magnitude = Math.sqrt(sumSquares);
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / magnitude);
        }
    }
}
