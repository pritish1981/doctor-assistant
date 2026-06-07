package com.superclinic.doctorassistant.ai.rag;

import com.superclinic.doctorassistant.persistence.entity.Doctor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Component
public class DoctorProfileDocumentBuilder {

    public IngestibleDocument toIngestibleDocument(Doctor doctor) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(RagMetadataKeys.CATEGORY, "doctor");
        metadata.put("specialty_code", doctor.getSpecialty().getCode());
        metadata.put("specialty_name", doctor.getSpecialty().getName());
        metadata.put("doctor_name", doctor.getFullName());
        metadata.put("languages", doctor.getLanguages());
        if (doctor.getConsultationFee() != null) {
            metadata.put("consultation_fee", doctor.getConsultationFee());
        }
        if (doctor.getRatingAvg() != null) {
            metadata.put("rating_avg", doctor.getRatingAvg());
        }

        return new IngestibleDocument(
                KnowledgeSourceType.DOCTOR_PROFILE,
                doctor.getId(),
                doctor.getFullName() + " — " + doctor.getSpecialty().getName(),
                buildProfileText(doctor),
                doctor.getActive(),
                metadata);
    }

    private String buildProfileText(Doctor doctor) {
        StringBuilder text = new StringBuilder();
        text.append("Doctor: ").append(doctor.getFullName()).append('\n');
        text.append("Specialty: ").append(doctor.getSpecialty().getName())
                .append(" (").append(doctor.getSpecialty().getCode()).append(")\n");
        appendIfPresent(text, "License", doctor.getLicenseNumber());
        appendIfPresent(text, "Email", doctor.getEmail());
        appendIfPresent(text, "Phone", doctor.getPhone());
        appendIfPresent(text, "Languages", doctor.getLanguages());
        if (doctor.getYearsExperience() != null) {
            text.append("Years of experience: ").append(doctor.getYearsExperience()).append('\n');
        }
        if (doctor.getRatingAvg() != null) {
            text.append("Average rating: ").append(doctor.getRatingAvg()).append('\n');
        }
        if (doctor.getConsultationFee() != null) {
            text.append("Consultation fee: ").append(doctor.getConsultationFee()).append('\n');
        }
        appendIfPresent(text, "Qualifications", doctor.getQualifications());
        appendIfPresent(text, "Bio", doctor.getBio());
        if (StringUtils.hasText(doctor.getSpecialty().getDescription())) {
            text.append("Specialty overview: ").append(doctor.getSpecialty().getDescription()).append('\n');
        }
        return text.toString().trim();
    }

    private void appendIfPresent(StringBuilder text, String label, String value) {
        if (StringUtils.hasText(value)) {
            text.append(label).append(": ").append(value).append('\n');
        }
    }
}
