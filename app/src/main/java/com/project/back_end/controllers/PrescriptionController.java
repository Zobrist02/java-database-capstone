package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.ClinicService;
import com.project.back_end.services.PrescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final ClinicService clinicService;

    public PrescriptionController(
            PrescriptionService prescriptionService,
            ClinicService clinicService) {
        this.prescriptionService = prescriptionService;
        this.clinicService = clinicService;
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> savePrescription(
            @PathVariable String token,
            @RequestBody Prescription prescription) {

        var tokenCheck = clinicService.validateToken(token, "doctor");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        return prescriptionService.savePrescription(prescription);
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<?> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {

        var tokenCheck = clinicService.validateToken(token, "doctor");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}