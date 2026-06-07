package com.superclinic.doctorassistant.scenario;

import com.superclinic.doctorassistant.ai.rag.KnowledgeSourceType;
import com.superclinic.doctorassistant.ai.rag.RagService;
import com.superclinic.doctorassistant.ai.rag.dto.RetrievedChunk;
import com.superclinic.doctorassistant.support.AbstractIntegrationTest;
import com.superclinic.doctorassistant.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@DisplayName("Scenario 6: FAQ lookup")
class RagFaqLookupIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RagService ragService;

    @BeforeEach
    void ingestFaqs() {
        ragService.ingestSourceType(KnowledgeSourceType.FAQ);
    }

    @Test
    void retrievesBookingFaqFromKnowledgeBase() {
        List<RetrievedChunk> results = ragService.search(
                "how do I book an appointment",
                Set.of(KnowledgeSourceType.FAQ),
                3);

        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().sourceType()).isEqualTo("FAQ");
        assertThat(results.getFirst().title()).containsIgnoringCase("book");
        assertThat(results.getFirst().content()).containsIgnoringCase("assistant");
    }

    @Test
    void retrievesClinicHoursFaq() {
        List<RetrievedChunk> results = ragService.search(
                "clinic operating hours",
                Set.of(KnowledgeSourceType.FAQ),
                3);

        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().content()).containsIgnoringCase("Monday to Saturday");
    }
}
