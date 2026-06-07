package com.superclinic.doctorassistant.ai.tools.dto;

import com.superclinic.doctorassistant.persistence.entity.Doctor;

import java.math.BigDecimal;
import java.util.UUID;

public record DoctorSearchResult(
        UUID id,
        String fullName,
        String specialtyCode,
        String specialtyName,
        String licenseNumber,
        String email,
        String phone,
        String languages,
        Short yearsExperience,
        BigDecimal ratingAvg,
        BigDecimal consultationFee,
        String bio
) {

    public static DoctorSearchResult from(Doctor doctor) {
        return new DoctorSearchResult(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getSpecialty().getCode(),
                doctor.getSpecialty().getName(),
                doctor.getLicenseNumber(),
                doctor.getEmail(),
                doctor.getPhone(),
                doctor.getLanguages(),
                doctor.getYearsExperience(),
                doctor.getRatingAvg(),
                doctor.getConsultationFee(),
                doctor.getBio());
    }
}
