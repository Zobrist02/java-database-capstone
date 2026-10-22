package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return List.of();

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> appointments =
                appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        List<String> bookedStartTimes = appointments.stream()
                .map(Appointment::getAppointmentTime)
                .filter(time -> time != null)
                .map(time -> time.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .toList();

        return doctor.getAvailableTimes().stream()
                .filter(slot -> {
                    String slotStart = slot.split("-")[0].trim();
                    return !bookedStartTimes.contains(slotStart);
                })
                .toList();
    }

    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) return -1;
            doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception exception) {
            return 0;
        }
    }

    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor.getId() == null || !doctorRepository.existsById(doctor.getId())) return -1;

            Doctor existing = doctorRepository.findById(doctor.getId()).orElseThrow();

            Doctor sameEmail = doctorRepository.findByEmail(doctor.getEmail());
            if (sameEmail != null && !sameEmail.getId().equals(doctor.getId())) return 0;

            if (doctor.getPassword() == null || doctor.getPassword().isBlank()) {
                doctor.setPassword(existing.getPassword());
            } else {
                doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
            }

            doctorRepository.save(doctor);
            return 1;
        } catch (Exception exception) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional
    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) return -1;
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception exception) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());

        if (doctor == null || !passwordEncoder.matches(login.getPassword(), doctor.getPassword())) {
            return message(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        return ResponseEntity.ok(Map.of(
                "message", "Login successful.",
                "token", tokenService.generateToken(doctor.getEmail(), "doctor")
        ));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        return doctorMap(doctorRepository.findByNameLike(name));
    }

    public Map<String, Object> filterDoctorsByNameSpecilityandTime(
            String name, String specialty, String amOrPm
    ) {
        return doctorMap(filterDoctorByTime(
                doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty),
                amOrPm
        ));
    }

    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        return doctorMap(filterDoctorByTime(doctorRepository.findByNameLike(name), amOrPm));
    }

    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        return doctorMap(doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty));
    }

    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        return doctorMap(filterDoctorByTime(doctorRepository.findBySpecialtyIgnoreCase(specialty), amOrPm));
    }

    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        return doctorMap(doctorRepository.findBySpecialtyIgnoreCase(specialty));
    }

    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        return doctorMap(filterDoctorByTime(doctorRepository.findAll(), amOrPm));
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (amOrPm == null || amOrPm.isBlank() || amOrPm.equalsIgnoreCase("null")) {
            return doctors;
        }

        return doctors.stream()
                .filter(doctor -> doctor.getAvailableTimes().stream().anyMatch(slot -> {
                    try {
                        LocalTime start = LocalTime.parse(slot.split("-")[0].trim());
                        return amOrPm.equalsIgnoreCase(start.getHour() < 12 ? "AM" : "PM");
                    } catch (Exception exception) {
                        return false;
                    }
                }))
                .toList();
    }

    private Map<String, Object> doctorMap(List<Doctor> doctors) {
        Map<String, Object> result = new HashMap<>();
        result.put("doctors", doctors);
        return result;
    }

    private ResponseEntity<Map<String, String>> message(HttpStatus status, String text) {
        return new ResponseEntity<>(Map.of("message", text), status);
    }
}