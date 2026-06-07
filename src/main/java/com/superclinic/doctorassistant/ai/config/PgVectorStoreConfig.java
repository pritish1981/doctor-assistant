package com.superclinic.doctorassistant.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class PgVectorStoreConfig {

    @Bean
    @Primary
    VectorStore vectorStore(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel,
            RagProperties ragProperties,
            @Value("${spring.ai.openai.embedding.options.dimensions:1536}") int dimensions,
            @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}") String tableName,
            @Value("${spring.ai.vectorstore.pgvector.schema-name:public}") String schemaName,
            @Value("${spring.ai.vectorstore.pgvector.index-type:HNSW}") String indexType,
            @Value("${spring.ai.vectorstore.pgvector.distance-type:COSINE_DISTANCE}") String distanceType,
            @Value("${spring.ai.vectorstore.pgvector.max-document-batch-size:10000}") int maxDocumentBatchSize) {

        PgVectorStore.PgIndexType pgIndexType = PgVectorStore.PgIndexType.valueOf(indexType.toUpperCase());
        PgVectorStore.PgDistanceType pgDistanceType = parseDistanceType(distanceType);

        log.info("""
                Configuring PgVectorStore: table={}.{}, dimensions={}, index={}, distance={}, topK={}, threshold={}\
                """,
                schemaName, tableName, dimensions, pgIndexType, pgDistanceType,
                ragProperties.getTopK(), ragProperties.getSimilarityThreshold());

        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .schemaName(schemaName)
                .vectorTableName(tableName)
                .dimensions(dimensions)
                .indexType(pgIndexType)
                .distanceType(pgDistanceType)
                .idType(PgVectorStore.PgIdType.UUID)
                .initializeSchema(false)
                .vectorTableValidationsEnabled(true)
                .removeExistingVectorStoreTable(false)
                .maxDocumentBatchSize(maxDocumentBatchSize)
                .build();
    }

    private PgVectorStore.PgDistanceType parseDistanceType(String distanceType) {
        return switch (distanceType.toUpperCase().replace('-', '_')) {
            case "COSINE_DISTANCE", "COSINE" -> PgVectorStore.PgDistanceType.COSINE_DISTANCE;
            case "EUCLIDEAN_DISTANCE", "L2", "EUCLIDEAN" -> PgVectorStore.PgDistanceType.EUCLIDEAN_DISTANCE;
            case "NEGATIVE_INNER_PRODUCT", "INNER_PRODUCT" -> PgVectorStore.PgDistanceType.NEGATIVE_INNER_PRODUCT;
            default -> PgVectorStore.PgDistanceType.COSINE_DISTANCE;
        };
    }
}
