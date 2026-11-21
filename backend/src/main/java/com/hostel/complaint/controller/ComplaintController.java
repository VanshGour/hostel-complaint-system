package com.hostel.complaint.controller;

import com.hostel.complaint.dto.*;
import com.hostel.complaint.exception.ResourceNotFoundException;
import com.hostel.complaint.model.*;
import com.hostel.complaint.repository.UserRepository;
import com.hostel.complaint.service.AuditLogService;
import com.hostel.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/complaints")
@Tag(name = "Complaints", description = "Core complaints lifecycle, upvoting, assigning, and status transitions")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    public ComplaintController(ComplaintService complaintService,
                               AuditLogService auditLogService,
                               UserRepository userRepository) {
        this.complaintService = complaintService;
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Raise a new complaint (Student role required)")
    public ResponseEntity<ComplaintResponse> createComplaint(@Valid @RequestBody ComplaintRequest req,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        User student = getCurrentUser(userDetails);
        return ResponseEntity.ok(complaintService.createComplaint(req, student));
    }

    @GetMapping
    @Operation(summary = "Browse complaints with filtering and pagination")
    public ResponseEntity<Page<ComplaintResponse>> getComplaints(
            @RequestParam(required = false) String block,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userDetails != null ? getCurrentUser(userDetails) : null;
        Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, sorting);

        return ResponseEntity.ok(complaintService.getFilteredComplaints(block, category, status, priority, currentUser, pageable));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get list of complaints created by logged-in student")
    public ResponseEntity<Page<ComplaintResponse>> getMyComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        User student = getCurrentUser(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(complaintService.getMyComplaints(student, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single complaint details")
    public ResponseEntity<ComplaintResponse> getComplaintDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userDetails != null ? getCurrentUser(userDetails) : null;
        return ResponseEntity.ok(complaintService.getComplaintDetails(id, currentUser));
    }

    @PostMapping("/{id}/upvote")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Upvote an existing complaint to prevent duplicates")
    public ResponseEntity<ComplaintResponse> upvoteComplaint(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User student = getCurrentUser(userDetails);
        return ResponseEntity.ok(complaintService.upvoteComplaint(id, student));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign complaint to maintenance staff (Optimistic Locking protected)")
    public ResponseEntity<ComplaintResponse> assignComplaint(
            @PathVariable Long id,
            @Valid @RequestBody AssignComplaintRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        User admin = getCurrentUser(userDetails);
        return ResponseEntity.ok(complaintService.assignComplaint(id, req, admin));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition complaint status (FSM validated)")
    public ResponseEntity<ComplaintResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        User actor = getCurrentUser(userDetails);
        return ResponseEntity.ok(complaintService.updateStatus(id, req, actor));
    }

    @GetMapping("/{id}/audit")
    @Operation(summary = "Get immutable audit trail of changes for a complaint")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getLogsForComplaint(id));
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
