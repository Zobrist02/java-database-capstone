package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final ClinicService sharedClinicService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            TokenService tokenService,
            ClinicService sharedClinicService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.sharedClinicService = sharedClinicService;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointment.setStatus(0);
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception exception) {
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        if (appointment.getId() == null) {
            return message(HttpStatus.BAD_REQUEST, "Appointment ID is required.");
        }

        Appointment existing = appointmentRepository.findById(appointment.getId()).orElse(null);
        if (existing == null) return message(HttpStatus.NOT_FOUND, "Appointment not found.");

        int valid = sharedClinicService.validateAppointment(appointment);
        if (valid != 1) return message(HttpStatus.BAD_REQUEST, "The requested time is unavailable.");

        existing.setDoctor(appointment.getDoctor());
        existing.setPatient(appointment.getPatient());
        existing.setAppointmentTime(appointment.getAppointmentTime());
        existing.setStatus(appointment.getStatus());
        appointmentRepository.save(existing);

        return message(HttpStatus.OK, "Appointment updated.");
    }

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        try {
            String identifier = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(identifier);
            Appointment appointment = appointmentRepository.findById(id).orElse(null);

            if (patient == null || appointment == null
                    || appointment.getPatient() == null
                    || !appointment.getPatient().getId().equals(patient.getId())) {
                return message(HttpStatus.NOT_FOUND, "Appointment not found for this patient.");
            }

            appointmentRepository.delete(appointment);
            return message(HttpStatus.OK, "Appointment cancelled.");
        } catch (Exception exception) {
            return message(HttpStatus.INTERNAL_SERVER_ERROR, "Could not cancel appointment.");
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        String identifier = tokenService.extractIdentifier(token);
        var doctor = doctorRepository.findByEmail(identifier);

        if (doctor == null) return Map.of("appointments", List.of());

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> appointments;
        if (pname == null || pname.isBlank() || pname.equalsIgnoreCase("null")) {
            appointments = appointmentRepository
                    .findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), start, end);
        } else {
            appointments = appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                            doctor.getId(), pname, start, end
                    );
        }

        return Map.of("appointments", appointments.stream().map(AppointmentDTO::from).toList());
    }

    private ResponseEntity<Map<String, String>> message(HttpStatus status, String text) {
        return new ResponseEntity<>(Map.of("message", text), status);
    }
}