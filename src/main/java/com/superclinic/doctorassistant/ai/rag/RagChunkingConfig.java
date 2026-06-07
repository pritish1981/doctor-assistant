package com.superclinic.doctorassistant.ai.rag;

import com.superclinic.doctorassistant.ai.config.RagProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RagChunkingConfig {

    @Bean
    TokenTextSplitter faqTextSplitter(RagProperties ragProperties) {
        int chunkSize = ragProperties.getChunking().getFaqChunkSize();
        log.info("RAG FAQ chunking: chunkSize={} (whole-document strategy)", chunkSize);
        return TokenTextSplitter.builder()
                .withChunkSize(chunkSize)
                .withMinChunkSizeChars(1)
                .withMinChunkLengthToEmbed(1)
                .withMaxNumChunks(100)
                .withKeepSeparator(true)
                .build();
    }

    @Bean
    TokenTextSplitter insurancePolicyTextSplitter(RagProperties ragProperties) {
        int chunkSize = ragProperties.getChunking().getInsuranceChunkSize();
        int overlap = ragProperties.getChunking().getInsuranceChunkOverlap();
        log.info("RAG insurance policy chunking: chunkSize={}, overlap={}", chunkSize, overlap);
        return TokenTextSplitter.builder()
                .withChunkSize(chunkSize)
                .withMinChunkSizeChars(Math.max(50, overlap))
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(500)
                .withKeepSeparator(true)
                .build();
    }

    @Bean
    TokenTextSplitter doctorProfileTextSplitter(RagProperties ragProperties) {
        int chunkSize = ragProperties.getChunking().getDoctorProfileChunkSize();
        log.info("RAG doctor profile chunking: chunkSize={} (single-chunk strategy)", chunkSize);
        return TokenTextSplitter.builder()
                .withChunkSize(chunkSize)
                .withMinChunkSizeChars(1)
                .withMinChunkLengthToEmbed(1)
                .withMaxNumChunks(500)
                .withKeepSeparator(true)
                .build();
    }
}
