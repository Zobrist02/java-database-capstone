package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        try {
            List<Prescription> existing =
                    prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());

            if (!existing.isEmpty()) {
                return new ResponseEntity<>(
                        Map.of("message", "A prescription already exists for this appointment."),
                        HttpStatus.CONFLICT
                );
            }

            prescriptionRepository.save(prescription);
            return new ResponseEntity<>(
                    Map.of("message", "Prescription saved."),
                    HttpStatus.CREATED
            );
        } catch (Exception exception) {
            return new ResponseEntity<>(
                    Map.of("message", "Could not save prescription."),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        try {
            List<Prescription> results =
                    prescriptionRepository.findByAppointmentId(appointmentId);

            if (results.isEmpty()) {
                return new ResponseEntity<>(
                        Map.of("message", "Prescription not found."),
                        HttpStatus.NOT_FOUND
                );
            }

            Map<String, Object> body = new HashMap<>();
            body.put("prescription", results.get(0));
            return ResponseEntity.ok(body);
        } catch (Exception exception) {
            return new ResponseEntity<>(
                    Map.of("message", "Could not retrieve prescription."),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}