package com.superclinic.doctorassistant.scenario;

import com.superclinic.doctorassistant.ai.tools.BookingTool;
import com.superclinic.doctorassistant.ai.tools.dto.AppointmentBookingResult;
import com.superclinic.doctorassistant.persistence.entity.enums.AvailabilityStatus;
import com.superclinic.doctorassistant.persistence.repository.AppointmentRepository;
import com.superclinic.doctorassistant.persistence.repository.AvailabilityRepository;
import com.superclinic.doctorassistant.support.AbstractIntegrationTest;
import com.superclinic.doctorassistant.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@DisplayName("Scenario 1: Book appointment")
class AppointmentBookingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BookingTool bookingTool;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Test
    void booksAppointmentAndMarksSlotAsBooked() {
        AppointmentBookingResult result = bookingTool.bookAppointment(
                PATIENT_ALICE,
                DOCTOR_SHARMA,
                SLOT_SHARMA_TOMORROW,
                "Routine cardiology check-up",
                "integration-test-book-001");

        assertThat(result.referenceCode()).isNotBlank();
        assertThat(result.doctorName()).contains("Sharma");
        assertThat(result.message()).contains("confirmed");

        assertThat(appointmentRepository.findByReferenceCode(result.referenceCode())).isPresent();

        var slot = availabilityRepository.findById(SLOT_SHARMA_TOMORROW).orElseThrow();
        assertThat(slot.getStatus()).isEqualTo(AvailabilityStatus.BOOKED);
    }
}
