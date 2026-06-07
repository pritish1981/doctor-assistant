package com.superclinic.doctorassistant.domain.patient;

import com.superclinic.doctorassistant.common.exception.BusinessValidationException;
import com.superclinic.doctorassistant.common.exception.ResourceNotFoundException;
import com.superclinic.doctorassistant.persistence.entity.Patient;
import com.superclinic.doctorassistant.persistence.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;

    public Patient getActivePatient(UUID patientId) {
        log.debug("Fetching active patient: {}", patientId);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        if (!Boolean.TRUE.equals(patient.getActive())) {
            throw new BusinessValidationException("Patient is inactive: " + patientId);
        }
        return patient;
    }

    public Patient getPatient(UUID patientId) {
        log.debug("Fetching patient: {}", patientId);
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
    }

    public Patient getByEmail(String email) {
        log.debug("Fetching patient by email");
        return patientRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", email));
    }

    public Patient getByExternalSubjectId(String externalSubjectId) {
        log.debug("Fetching patient by external subject id");
        return patientRepository.findByExternalSubjectIdAndActiveTrue(externalSubjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", externalSubjectId));
    }
}
