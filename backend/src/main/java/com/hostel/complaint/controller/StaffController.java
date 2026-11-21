package com.hostel.complaint.controller;

import com.hostel.complaint.dto.ComplaintResponse;
import com.hostel.complaint.exception.ResourceNotFoundException;
import com.hostel.complaint.model.User;
import com.hostel.complaint.repository.UserRepository;
import com.hostel.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
@Tag(name = "Staff Tasks", description = "Endpoints for maintenance staff task queue and progress")
public class StaffController {

    private final ComplaintService complaintService;
    private final UserRepository userRepository;

    public StaffController(ComplaintService complaintService, UserRepository userRepository) {
        this.complaintService = complaintService;
        this.userRepository = userRepository;
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Get active tasks assigned to the current staff member")
    public ResponseEntity<Page<ComplaintResponse>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        User staff = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated staff not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastStatusChangeAt"));
        return ResponseEntity.ok(complaintService.getStaffTasks(staff, pageable));
    }
}
