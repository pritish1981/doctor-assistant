package com.superclinic.doctorassistant.persistence.repository;

import com.superclinic.doctorassistant.persistence.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByEmail(String email);

    Optional<Patient> findByEmailAndActiveTrue(String email);

    Optional<Patient> findByExternalSubjectId(String externalSubjectId);

    Optional<Patient> findByExternalSubjectIdAndActiveTrue(String externalSubjectId);

    boolean existsByEmail(String email);
}
