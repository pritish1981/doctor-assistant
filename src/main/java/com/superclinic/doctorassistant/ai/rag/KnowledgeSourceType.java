package com.superclinic.doctorassistant.ai.rag;

public enum KnowledgeSourceType {

    DOCTOR_PROFILE("Doctor profile"),
    FAQ("Clinic FAQ"),
    INSURANCE_POLICY("Insurance policy"),
    APPOINTMENT_POLICY("Appointment policy");

    private final String displayName;

    KnowledgeSourceType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
