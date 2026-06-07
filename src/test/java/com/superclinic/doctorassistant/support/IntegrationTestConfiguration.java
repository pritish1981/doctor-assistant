package com.superclinic.doctorassistant.support;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestConfiguration
public class IntegrationTestConfiguration {

    /** Prevents OpenAI chat calls during integration tests (Mockito). */
    @MockitoBean
    OpenAiChatModel openAiChatModel;

    @Bean
    @Primary
    EmbeddingModel testEmbeddingModel() {
        return new DeterministicTestEmbeddingModel(1536);
    }
}
