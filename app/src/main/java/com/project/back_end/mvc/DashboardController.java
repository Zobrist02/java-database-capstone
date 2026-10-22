package com.project.back_end.mvc;

import com.project.back_end.services.ClinicService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DashboardController {

    private final ClinicService clinicService;

    public DashboardController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, String>> result = clinicService.validateToken(token, "admin");

        if (result.getStatusCode().is2xxSuccessful()) {
            return "admin/adminDashboard";
        }

        return "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, String>> result = clinicService.validateToken(token, "doctor");

        if (result.getStatusCode().is2xxSuccessful()) {
            return "doctor/doctorDashboard";
        }

        return "redirect:/";
    }
}