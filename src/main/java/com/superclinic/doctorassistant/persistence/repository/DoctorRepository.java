package com.superclinic.doctorassistant.persistence.repository;

import com.superclinic.doctorassistant.persistence.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    @Query("""
            SELECT d FROM Doctor d
            JOIN FETCH d.specialty s
            WHERE d.active = true
              AND LOWER(d.fullName) LIKE LOWER(CONCAT('%', :name, '%'))
            ORDER BY d.fullName ASC
            """)
    List<Doctor> findDoctorByName(@Param("name") String name);

    @Query("""
            SELECT d FROM Doctor d
            JOIN FETCH d.specialty s
            WHERE d.active = true
              AND (
                    LOWER(s.code) = LOWER(:speciality)
                 OR LOWER(s.name) = LOWER(:speciality)
                 OR LOWER(s.code) LIKE LOWER(CONCAT('%', :speciality, '%'))
                 OR LOWER(s.name) LIKE LOWER(CONCAT('%', :speciality, '%'))
                 OR LOWER(:speciality) LIKE LOWER(CONCAT('%', s.name, '%'))
              )
            ORDER BY d.ratingAvg DESC, d.fullName ASC
            """)
    List<Doctor> findDoctorsBySpeciality(@Param("speciality") String speciality);

    @Query("""
            SELECT d FROM Doctor d
            JOIN FETCH d.specialty
            WHERE d.id = :id
              AND d.active = true
            """)
    Optional<Doctor> findActiveById(@Param("id") UUID id);

    @Query("""
            SELECT d FROM Doctor d
            JOIN FETCH d.specialty s
            WHERE d.active = true
            ORDER BY s.name ASC, d.fullName ASC
            """)
    List<Doctor> findAllActiveWithSpecialty();
}
