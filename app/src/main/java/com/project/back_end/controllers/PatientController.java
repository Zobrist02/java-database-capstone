package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.ClinicService;
import com.project.back_end.services.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final ClinicService clinicService;

    public PatientController(
            PatientService patientService,
            ClinicService clinicService) {
        this.patientService = patientService;
        this.clinicService = clinicService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getPatientDetails(
            @PathVariable String token) {

        var tokenCheck = clinicService.validateToken(token, "patient");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        return patientService.getPatientDetails(token);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(
            @RequestBody Patient patient) {

        if (!clinicService.validatePatient(patient)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Patient with email id or phone no already exist"));
        }

        int result = patientService.createPatient(patient);

        if (result == 1) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Signup successful"));
        }

        return ResponseEntity.internalServerError()
                .body(Map.of("message", "Internal server error"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> patientLogin(
            @RequestBody Login login) {
        return clinicService.validatePatientLogin(login);
    }

    // Supports both /patient/{id}/{token}
    // and the lab example /patient/{id}/patient/{token}.
    @GetMapping({"/{id}/{token}", "/{id}/{user}/{token}"})
    public ResponseEntity<?> getPatientAppointments(
            @PathVariable Long id,
            @PathVariable(required = false) String user,
            @PathVariable String token) {

        var tokenCheck = clinicService.validateToken(token, "patient");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        return patientService.getPatientAppointment(id, token);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<?> filterPatientAppointments(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {

        var tokenCheck = clinicService.validateToken(token, "patient");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        if (!"scheduled".equalsIgnoreCase(condition)
                && !"completed".equalsIgnoreCase(condition)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Condition must be scheduled or completed"));
        }

        return clinicService.filterPatient(condition, name, token);
    }
}