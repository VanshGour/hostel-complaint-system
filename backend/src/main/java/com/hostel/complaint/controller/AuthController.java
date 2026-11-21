package com.hostel.complaint.controller;

import com.hostel.complaint.dto.AuthRequest;
import com.hostel.complaint.dto.AuthResponse;
import com.hostel.complaint.dto.RegisterRequest;
import com.hostel.complaint.model.Role;
import com.hostel.complaint.model.User;
import com.hostel.complaint.repository.UserRepository;
import com.hostel.complaint.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, and staff directory")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @Operation(summary = "Register user (restricted to college domain email)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and obtain JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/staff-list")
    @Operation(summary = "List active maintenance staff for assignment dropdowns")
    public ResponseEntity<List<Map<String, Object>>> getActiveStaff() {
        List<User> staffMembers = userRepository.findByRoleAndActiveTrue(Role.STAFF);
        List<Map<String, Object>> staffDto = staffMembers.stream()
                .map(s -> Map.<String, Object>of(
                        "id", s.getId(),
                        "name", s.getFullName(),
                        "email", s.getEmail(),
                        "tradeSpecialty", s.getTradeSpecialty() != null ? s.getTradeSpecialty() : "GENERAL"
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(staffDto);
    }
}
