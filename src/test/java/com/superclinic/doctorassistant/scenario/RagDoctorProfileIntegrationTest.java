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
@DisplayName("Scenario 5: RAG doctor profile lookup")
class RagDoctorProfileIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RagService ragService;

    @BeforeEach
    void ingestDoctorProfiles() {
        ragService.ingestSourceType(KnowledgeSourceType.DOCTOR_PROFILE);
    }

    @Test
    void retrievesCardiologistProfileFromKnowledgeBase() {
        List<RetrievedChunk> results = ragService.search(
                "cardiologist Rajesh Sharma experience",
                Set.of(KnowledgeSourceType.DOCTOR_PROFILE),
                3);

        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().sourceType()).isEqualTo("DOCTOR_PROFILE");
        assertThat(results.getFirst().title()).contains("Sharma");
        assertThat(results.getFirst().content()).containsIgnoringCase("cardiologist");
        assertThat(results.getFirst().content()).contains("18 years");
    }
}
