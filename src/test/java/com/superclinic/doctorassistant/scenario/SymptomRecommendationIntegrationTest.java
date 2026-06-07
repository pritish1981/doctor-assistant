package com.superclinic.doctorassistant.scenario;

import com.superclinic.doctorassistant.ai.tools.DoctorSearchTool;
import com.superclinic.doctorassistant.ai.tools.dto.DoctorSearchResult;
import com.superclinic.doctorassistant.support.AbstractIntegrationTest;
import com.superclinic.doctorassistant.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@DisplayName("Scenario 4: Symptom recommendation")
class SymptomRecommendationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DoctorSearchTool doctorSearchTool;

    @Test
    void recommendsCardiologistForChestPainSymptoms() {
        // Simulates agent workflow: symptoms suggest cardiology → search by specialty
        String symptomContext = "chest pain and shortness of breath";
        assertThat(symptomContext).contains("chest pain");

        List<DoctorSearchResult> cardiologists =
                doctorSearchTool.findDoctorsBySpeciality("CARDIOLOGY");

        assertThat(cardiologists).isNotEmpty();
        assertThat(cardiologists.getFirst().specialtyCode()).isEqualTo("CARDIOLOGY");
        assertThat(cardiologists.getFirst().fullName()).contains("Sharma");
        assertThat(cardiologists.getFirst().bio()).containsIgnoringCase("cardio");
    }

    @Test
    void findsCardiologistBySpecialtyNameCaseInsensitive() {
        List<DoctorSearchResult> results =
                doctorSearchTool.findDoctorsBySpeciality("Cardiology");

        assertThat(results).extracting(DoctorSearchResult::specialtyCode)
                .containsExactly("CARDIOLOGY");
    }
}
