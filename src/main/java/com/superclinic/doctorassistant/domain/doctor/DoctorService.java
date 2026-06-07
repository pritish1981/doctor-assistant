package com.superclinic.doctorassistant.domain.doctor;

import com.superclinic.doctorassistant.common.exception.BusinessValidationException;
import com.superclinic.doctorassistant.common.exception.ResourceNotFoundException;
import com.superclinic.doctorassistant.persistence.entity.Doctor;
import com.superclinic.doctorassistant.persistence.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public List<Doctor> findDoctorByName(String name) {
        validateSearchTerm(name, "Doctor name");
        log.debug("Searching doctors by name: {}", name);
        List<Doctor> doctors = doctorRepository.findDoctorByName(name.trim());
        log.info("Found {} doctor(s) matching name '{}'", doctors.size(), name);
        return doctors;
    }

    public List<Doctor> findDoctorsBySpeciality(String speciality) {
        validateSearchTerm(speciality, "Speciality");
        log.debug("Searching doctors by speciality: {}", speciality);
        List<Doctor> doctors = doctorRepository.findDoctorsBySpeciality(speciality.trim());
        log.info("Found {} doctor(s) for speciality '{}'", doctors.size(), speciality);
        return doctors;
    }

    public Doctor getActiveDoctor(UUID doctorId) {
        log.debug("Fetching active doctor: {}", doctorId);
        return doctorRepository.findActiveById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
    }

    public Doctor getDoctor(UUID doctorId) {
        log.debug("Fetching doctor: {}", doctorId);
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
    }

    private void validateSearchTerm(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessValidationException(fieldName + " must not be blank");
        }
    }
}
