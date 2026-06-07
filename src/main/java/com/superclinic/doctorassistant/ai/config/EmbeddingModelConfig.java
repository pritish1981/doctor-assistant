package com.superclinic.doctorassistant.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class EmbeddingModelConfig {

    @Bean
    EmbeddingModelInfo embeddingModelInfo(
            EmbeddingModel embeddingModel,
            @Value("${spring.ai.openai.embedding.options.model:text-embedding-3-small}") String model,
            @Value("${spring.ai.openai.embedding.options.dimensions:1536}") int dimensions) {
        log.info("OpenAI EmbeddingModel ready: model={}, dimensions={}, implementation={}",
                model, dimensions, embeddingModel.getClass().getSimpleName());
        return new EmbeddingModelInfo(model, dimensions, embeddingModel.getClass().getSimpleName());
    }

    public record EmbeddingModelInfo(String model, int dimensions, String implementation) {
    }
}
