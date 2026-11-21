package com.hostel.complaint.service;

import com.hostel.complaint.dto.*;
import com.hostel.complaint.exception.*;
import com.hostel.complaint.model.*;
import com.hostel.complaint.repository.ComplaintRepository;
import com.hostel.complaint.repository.ComplaintUpvoteRepository;
import com.hostel.complaint.repository.UserRepository;
import com.hostel.complaint.statemachine.ComplaintStateMachine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintUpvoteRepository upvoteRepository;
    private final UserRepository userRepository;
    private final ComplaintStateMachine stateMachine;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public ComplaintService(ComplaintRepository complaintRepository,
                            ComplaintUpvoteRepository upvoteRepository,
                            UserRepository userRepository,
                            ComplaintStateMachine stateMachine,
                            AuditLogService auditLogService,
                            NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.upvoteRepository = upvoteRepository;
        this.userRepository = userRepository;
        this.stateMachine = stateMachine;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest req, User creator) {
        Complaint complaint = new Complaint();
        complaint.setTitle(req.getTitle().trim());
        complaint.setDescription(req.getDescription().trim());
        complaint.setCategory(req.getCategory());
        complaint.setHostelBlock(req.getHostelBlock());
        complaint.setRoomOrArea(req.getRoomOrArea());
        complaint.setPriority(req.getPriority() != null ? req.getPriority() : Priority.MEDIUM);
        complaint.setStatus(ComplaintStatus.OPEN);
        complaint.setPhotoUrl(req.getPhotoUrl());
        complaint.setCreatedBy(creator);

        Complaint saved = complaintRepository.save(complaint);

        auditLogService.recordLog(
                saved, creator, null, ComplaintStatus.OPEN, "CREATED", "Initial complaint submission"
        );

        return ComplaintResponse.fromEntity(saved, false);
    }

    @Transactional
    public ComplaintResponse upvoteComplaint(Long complaintId, User student) {
        Complaint complaint = getEntityById(complaintId);

        if (upvoteRepository.existsByComplaintIdAndStudentId(complaintId, student.getId())) {
            throw new DuplicateVoteException("You have already upvoted this complaint.");
        }

        // Record Upvote
        ComplaintUpvote upvote = new ComplaintUpvote(complaint, student);
        upvoteRepository.save(upvote);

        // Atomic update of the counter
        complaintRepository.incrementUpvoteCount(complaintId);
        complaint.setUpvoteCount(complaint.getUpvoteCount() + 1);

        return ComplaintResponse.fromEntity(complaint, true);
    }

    @Transactional
    public ComplaintResponse assignComplaint(Long complaintId, AssignComplaintRequest req, User admin) {
        Complaint complaint = getEntityById(complaintId);

        // 1. Explicit Optimistic Locking Check
        if (!Objects.equals(complaint.getVersion(), req.getVersion())) {
            throw new OptimisticLockConflictException(
                    String.format("Optimistic Lock Error: Expected version %d, but found %d. The ticket was modified elsewhere.",
                            req.getVersion(), complaint.getVersion())
            );
        }

        // 2. Validate Target Staff
        User staff = userRepository.findById(req.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found with ID: " + req.getStaffId()));

        if (staff.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("Designated user is not a maintenance STAFF member.");
        }

        // 3. Finite State Machine Validation
        ComplaintStatus previousStatus = complaint.getStatus();
        stateMachine.validateTransition(complaint, ComplaintStatus.ASSIGNED, admin, req.getNotes());

        // 4. Update Assignment and Status
        complaint.setAssignedTo(staff);
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        if (req.getPriority() != null) {
            complaint.setPriority(req.getPriority());
        }

        Complaint updated = complaintRepository.save(complaint);

        // 5. Audit Log and Notification
        auditLogService.recordLog(
                updated, admin, previousStatus, ComplaintStatus.ASSIGNED, "ASSIGNED",
                String.format("Assigned to staff '%s' (%s). %s", staff.getFullName(), staff.getTradeSpecialty(),
                        req.getNotes() != null ? req.getNotes() : "")
        );

        notificationService.notifyStaffAssignment(updated);

        return ComplaintResponse.fromEntity(updated, false);
    }

    @Transactional
    public ComplaintResponse updateStatus(Long complaintId, UpdateStatusRequest req, User actor) {
        Complaint complaint = getEntityById(complaintId);

        // 1. Explicit Optimistic Locking Check
        if (!Objects.equals(complaint.getVersion(), req.getVersion())) {
            throw new OptimisticLockConflictException(
                    String.format("Optimistic Lock Error: Expected version %d, but current version is %d.",
                            req.getVersion(), complaint.getVersion())
            );
        }

        // 2. State Machine Validation
        ComplaintStatus previousStatus = complaint.getStatus();
        stateMachine.validateTransition(complaint, req.getTargetStatus(), actor, req.getNotes());

        // 3. Apply status update
        complaint.setStatus(req.getTargetStatus());
        if (req.getNotes() != null) {
            if (req.getTargetStatus() == ComplaintStatus.RESOLVED) {
                complaint.setResolutionNotes(req.getNotes());
            }
        }
        if (req.getPhotoUrl() != null && req.getTargetStatus() == ComplaintStatus.RESOLVED) {
            complaint.setResolutionPhotoUrl(req.getPhotoUrl());
        }

        Complaint updated = complaintRepository.save(complaint);

        // 4. Record Audit Log
        auditLogService.recordLog(
                updated, actor, previousStatus, req.getTargetStatus(), "STATUS_CHANGE", req.getNotes()
        );

        // 5. Trigger Notifications
        notificationService.notifyStatusChange(updated, req.getNotes());

        return ComplaintResponse.fromEntity(updated, false);
    }

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getFilteredComplaints(String block, Category category, 
                                                         ComplaintStatus status, Priority priority, 
                                                         User currentUser, Pageable pageable) {
        Page<Complaint> page = complaintRepository.findFilteredComplaints(block, category, status, priority, pageable);

        Set<Long> userUpvotedIds = currentUser != null && currentUser.getRole() == Role.STUDENT
                ? upvoteRepository.findByStudentId(currentUser.getId()).stream()
                    .map(u -> u.getComplaint().getId())
                    .collect(Collectors.toSet())
                : Set.of();

        return page.map(c -> ComplaintResponse.fromEntity(c, userUpvotedIds.contains(c.getId())));
    }

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getMyComplaints(User student, Pageable pageable) {
        Page<Complaint> page = complaintRepository.findByCreatedById(student.getId(), pageable);
        return page.map(c -> ComplaintResponse.fromEntity(c, true));
    }

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getStaffTasks(User staff, Pageable pageable) {
        List<ComplaintStatus> activeStatuses = List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS);
        Page<Complaint> page = complaintRepository.findByAssignedToIdAndStatusIn(staff.getId(), activeStatuses, pageable);
        return page.map(c -> ComplaintResponse.fromEntity(c, false));
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintDetails(Long id, User currentUser) {
        Complaint complaint = getEntityById(id);
        boolean hasUpvoted = currentUser != null && upvoteRepository.existsByComplaintIdAndStudentId(id, currentUser.getId());
        return ComplaintResponse.fromEntity(complaint, hasUpvoted);
    }

    public Complaint getEntityById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));
    }
}
