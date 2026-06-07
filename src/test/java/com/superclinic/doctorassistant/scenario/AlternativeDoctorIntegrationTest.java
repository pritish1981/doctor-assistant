package com.superclinic.doctorassistant.scenario;

import com.superclinic.doctorassistant.ai.tools.AvailabilityTool;
import com.superclinic.doctorassistant.ai.tools.DoctorSearchTool;
import com.superclinic.doctorassistant.ai.tools.dto.AvailabilityResponse;
import com.superclinic.doctorassistant.ai.tools.dto.DoctorSearchResult;
import com.superclinic.doctorassistant.support.AbstractIntegrationTest;
import com.superclinic.doctorassistant.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@DisplayName("Scenario 3: Alternative doctor")
class AlternativeDoctorIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DoctorSearchTool doctorSearchTool;

    @Autowired
    private AvailabilityTool availabilityTool;

    @Test
    void findsAlternativeDoctorWithAvailabilityWhenPreferredDoctorHasNoSlots() {
        AvailabilityResponse preferredDoctor = availabilityTool.getAvailability(
                DOCTOR_SHARMA,
                isoDate(inDays(5)));
        assertThat(preferredDoctor.totalSlots()).isZero();

        List<DoctorSearchResult> dermatologists =
                doctorSearchTool.findDoctorsBySpeciality("DERMATOLOGY");
        assertThat(dermatologists).hasSize(1);
        assertThat(dermatologists.getFirst().fullName()).contains("Nair");

        AvailabilityResponse alternativeAvailability = availabilityTool.getAvailability(
                dermatologists.getFirst().id(),
                isoDate(inDays(2)));

        assertThat(alternativeAvailability.totalSlots()).isGreaterThan(0);
        assertThat(alternativeAvailability.slots().getFirst().slotId())
                .isEqualTo(SLOT_NAIR_DAY2);
    }
}
