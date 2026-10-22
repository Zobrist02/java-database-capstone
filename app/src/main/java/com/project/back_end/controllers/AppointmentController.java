package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.ClinicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ClinicService clinicService;

    public AppointmentController(
            AppointmentService appointmentService,
            ClinicService clinicService) {
        this.appointmentService = appointmentService;
        this.clinicService = clinicService;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token) {

        var tokenCheck = clinicService.validateToken(token, "doctor");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        try {
            return ResponseEntity.ok(appointmentService.getAppointment(
                    patientName, LocalDate.parse(date), token));
        } catch (DateTimeParseException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Date must use YYYY-MM-DD format"));
        }
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> bookAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment) {

        var tokenCheck = clinicService.validateToken(token, "patient");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        int validationResult = clinicService.validateAppointment(appointment);
        if (validationResult != 1) {
            HttpStatus status = validationResult == -1
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(Map.of("message", "Appointment is invalid or unavailable"));
        }

        if (appointmentService.bookAppointment(appointment) == 1) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Appointment booked"));
        }

        return ResponseEntity.internalServerError()
                .body(Map.of("message", "Could not book appointment"));
    }

    @PutMapping("/{token}")
    public ResponseEntity<?> updateAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment) {

        var tokenCheck = clinicService.validateToken(token, "patient");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token) {

        var tokenCheck = clinicService.validateToken(token, "patient");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        return appointmentService.cancelAppointment(id, token);
    }
}