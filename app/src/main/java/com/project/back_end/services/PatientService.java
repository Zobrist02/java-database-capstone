package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public PatientService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public int createPatient(Patient patient) {
        try {
            if (patientRepository.findByEmail(patient.getEmail()) != null
                    || !patientRepository.findAllByPhone(patient.getPhone()).isEmpty()) {
                return 0;
            }

            patient.setPassword(passwordEncoder.encode(patient.getPassword()));
            patientRepository.save(patient);
            return 1;
        } catch (Exception exception) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        String identifier = tokenService.extractIdentifier(token);
        Patient patient = patientRepository.findByEmail(identifier);

        if (patient == null || !patient.getId().equals(id)) {
            return response(HttpStatus.UNAUTHORIZED, "Not authorized to view these appointments.");
        }

        var appointments = appointmentRepository.findByPatientId(id).stream()
                .map(AppointmentDTO::from)
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("appointments", appointments);
        return ResponseEntity.ok(body);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Integer status = statusFor(condition);
        if (status == null) return response(HttpStatus.BAD_REQUEST, "Use 'scheduled' or 'completed'.");

        var appointments = appointmentRepository
                .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status)
                .stream().map(AppointmentDTO::from).toList();

        Map<String, Object> body = new HashMap<>();
        body.put("appointments", appointments);
        return ResponseEntity.ok(body);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        var appointments = appointmentRepository
                .filterByDoctorNameAndPatientId(name, patientId)
                .stream().map(AppointmentDTO::from).toList();

        Map<String, Object> body = new HashMap<>();
        body.put("appointments", appointments);
        return ResponseEntity.ok(body);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(
            String condition, String name, long patientId
    ) {
        Integer status = statusFor(condition);
        if (status == null) return response(HttpStatus.BAD_REQUEST, "Use 'scheduled' or 'completed'.");

        var appointments = appointmentRepository
                .filterByDoctorNameAndPatientIdAndStatus(name, patientId, status)
                .stream().map(AppointmentDTO::from).toList();

        Map<String, Object> body = new HashMap<>();
        body.put("appointments", appointments);
        return ResponseEntity.ok(body);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        String identifier = tokenService.extractIdentifier(token);
        Patient patient = patientRepository.findByEmail(identifier);

        if (patient == null) return response(HttpStatus.NOT_FOUND, "Patient not found.");

        Map<String, Object> body = new HashMap<>();
        body.put("patient", patient);
        return ResponseEntity.ok(body);
    }

    private Integer statusFor(String condition) {
        if (condition == null) return null;
        return switch (condition.toLowerCase()) {
            case "scheduled" -> 0;
            case "completed" -> 1;
            default -> null;
        };
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        return new ResponseEntity<>(Map.of("message", message), status);
    }
}