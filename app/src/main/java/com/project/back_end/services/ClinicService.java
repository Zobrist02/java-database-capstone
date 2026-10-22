package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ClinicService {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final PasswordEncoder passwordEncoder;

    public ClinicService(
            TokenService tokenService,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            PatientService patientService,
            PasswordEncoder passwordEncoder
    ) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        if (!tokenService.validateToken(token, user)) {
            return new ResponseEntity<>(
                    Map.of("message", "Invalid or expired token."),
                    HttpStatus.UNAUTHORIZED
            );
        }

        return ResponseEntity.ok(Map.of());
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());

        if (admin == null || !passwordEncoder.matches(receivedAdmin.getPassword(), admin.getPassword())) {
            return new ResponseEntity<>(
                    Map.of("message", "Invalid credentials."),
                    HttpStatus.UNAUTHORIZED
            );
        }

        return ResponseEntity.ok(Map.of(
                "message", "Login successful.",
                "token", tokenService.generateToken(admin.getUsername(), "admin")
        ));
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        List<?> doctors;

        boolean hasName = hasValue(name);
        boolean hasSpecialty = hasValue(specialty);
        boolean hasTime = hasValue(time);

        if (hasName && hasSpecialty && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        } else if (hasName && hasSpecialty) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        } else if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(name, time);
        } else if (hasSpecialty && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        } else if (hasName) {
            return doctorService.findDoctorByName(name);
        } else if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(specialty);
        } else if (hasTime) {
            return doctorService.filterDoctorsByTime(time);
        }

        doctors = doctorService.getDoctors();
        return Map.of("doctors", doctors);
    }

    public int validateAppointment(Appointment appointment) {
        if (appointment.getDoctor() == null || appointment.getAppointmentTime() == null) return 0;

        var doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);
        if (doctor == null) return -1;

        LocalDate date = appointment.getAppointmentTime().toLocalDate();
        String requestedTime = appointment.getAppointmentTime().toLocalTime().toString();

        boolean available = doctorService.getDoctorAvailability(doctor.getId(), date).stream()
                .anyMatch(slot -> slot.startsWith(requestedTime));

        return available ? 1 : 0;
    }

    public boolean validatePatient(Patient patient) {
        if (patientRepository.findByEmail(patient.getEmail()) != null) return false;
        return patientRepository.findAllByPhone(patient.getPhone()).isEmpty();
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Patient patient = patientRepository.findByEmail(login.getIdentifier());

        if (patient == null || !passwordEncoder.matches(login.getPassword(), patient.getPassword())) {
            return new ResponseEntity<>(
                    Map.of("message", "Invalid credentials."),
                    HttpStatus.UNAUTHORIZED
            );
        }

        return ResponseEntity.ok(Map.of(
                "message", "Login successful.",
                "token", tokenService.generateToken(patient.getEmail(), "patient")
        ));
    }

    public ResponseEntity<Map<String, Object>> filterPatient(
            String condition, String name, String token
    ) {
        String identifier = tokenService.extractIdentifier(token);
        Patient patient = patientRepository.findByEmail(identifier);

        if (patient == null) {
            return new ResponseEntity<>(
                    Map.of("message", "Patient not found."),
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (hasValue(condition) && hasValue(name)) {
            return patientService.filterByDoctorAndCondition(condition, name, patient.getId());
        } else if (hasValue(condition)) {
            return patientService.filterByCondition(condition, patient.getId());
        } else if (hasValue(name)) {
            return patientService.filterByDoctor(name, patient.getId());
        }

        return patientService.getPatientAppointment(patient.getId(), token);
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank() && !value.equalsIgnoreCase("null");
    }
}