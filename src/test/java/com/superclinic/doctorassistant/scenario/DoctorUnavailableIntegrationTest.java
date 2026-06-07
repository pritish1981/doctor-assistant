package com.superclinic.doctorassistant.scenario;

import com.superclinic.doctorassistant.ai.tools.AvailabilityTool;
import com.superclinic.doctorassistant.ai.tools.dto.AlternativeSlotsResponse;
import com.superclinic.doctorassistant.ai.tools.dto.AvailabilityResponse;
import com.superclinic.doctorassistant.support.AbstractIntegrationTest;
import com.superclinic.doctorassistant.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@DisplayName("Scenario 2: Doctor unavailable")
class DoctorUnavailableIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AvailabilityTool availabilityTool;

    @Test
    void returnsNoSlotsWhenDoctorUnavailableOnRequestedDate() {
        AvailabilityResponse response = availabilityTool.getAvailability(
                DOCTOR_SHARMA,
                isoDate(inDays(5)));

        assertThat(response.totalSlots()).isZero();
        assertThat(response.slots()).isEmpty();
    }

    @Test
    void suggestsAlternativeSlotsOnNearbyDates() {
        AlternativeSlotsResponse alternatives = availabilityTool.suggestAlternativeSlots(
                DOCTOR_SHARMA,
                isoDate(inDays(5)),
                7,
                5);

        assertThat(alternatives.slotsAvailableOnRequestedDate()).isFalse();
        assertThat(alternatives.totalAlternatives()).isGreaterThan(0);
        assertThat(alternatives.alternatives()).isNotEmpty();
    }
}
