package com.project.back_end.repo;

import com.project.back_end.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Patient findByEmail(String email);

    // Included as requested. Prefer findByEmail and findAllByPhone
    // when phone numbers are not unique.
    Patient findByEmailOrPhone(String email, String phone);

    List<Patient> findAllByPhone(String phone);
}