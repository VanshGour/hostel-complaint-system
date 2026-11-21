package com.hostel.complaint;

import com.hostel.complaint.dto.AssignComplaintRequest;
import com.hostel.complaint.exception.DuplicateVoteException;
import com.hostel.complaint.exception.OptimisticLockConflictException;
import com.hostel.complaint.model.*;
import com.hostel.complaint.repository.ComplaintRepository;
import com.hostel.complaint.repository.ComplaintUpvoteRepository;
import com.hostel.complaint.repository.UserRepository;
import com.hostel.complaint.service.AuditLogService;
import com.hostel.complaint.service.ComplaintService;
import com.hostel.complaint.service.NotificationService;
import com.hostel.complaint.statemachine.ComplaintStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintUpvoteRepository upvoteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ComplaintStateMachine stateMachine;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ComplaintService complaintService;

    private Complaint testComplaint;
    private User student;
    private User admin;
    private User staff;

    @BeforeEach
    void setUp() {
        student = new User("rahul@campus.edu", "pass", "Rahul", "123", Role.STUDENT, "Block A", "304", null);
        student.setId(1L);

        admin = new User("warden@campus.edu", "pass", "Warden", "456", Role.ADMIN, "Block A", "Office", null);
        admin.setId(2L);

        staff = new User("plumber@campus.edu", "pass", "Plumber", "789", Role.STAFF, "Campus", "Maint", "PLUMBING");
        staff.setId(3L);

        testComplaint = new Complaint();
        testComplaint.setId(10L);
        testComplaint.setTitle("Broken Tap");
        testComplaint.setDescription("Water pouring non-stop");
        testComplaint.setStatus(ComplaintStatus.OPEN);
        testComplaint.setHostelBlock("Block A");
        testComplaint.setRoomOrArea("304");
        testComplaint.setCreatedBy(student);
        testComplaint.setVersion(2L); // Current version is 2
    }

    @Test
    @DisplayName("Optimistic Lock conflict detected when admin version is stale")
    void testOptimisticLockingThrowsOnStaleVersion() {
        when(complaintRepository.findById(10L)).thenReturn(Optional.of(testComplaint));

        AssignComplaintRequest req = new AssignComplaintRequest();
        req.setStaffId(3L);
        req.setVersion(1L); // Stale version! (Current in DB is 2)

        OptimisticLockConflictException ex = assertThrows(
            OptimisticLockConflictException.class,
            () -> complaintService.assignComplaint(10L, req, admin)
        );

        assertTrue(ex.getMessage().contains("Optimistic Lock Error: Expected version 1, but found 2"));
        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("Upvoting an already upvoted complaint throws DuplicateVoteException")
    void testDuplicateUpvoteThrows() {
        when(complaintRepository.findById(10L)).thenReturn(Optional.of(testComplaint));
        when(upvoteRepository.existsByComplaintIdAndStudentId(10L, 1L)).thenReturn(true);

        assertThrows(
            DuplicateVoteException.class,
            () -> complaintService.upvoteComplaint(10L, student)
        );

        verify(upvoteRepository, never()).save(any());
        verify(complaintRepository, never()).incrementUpvoteCount(any());
    }

    @Test
    @DisplayName("Valid first-time upvote succeeds and increments counter")
    void testSuccessfulUpvote() {
        when(complaintRepository.findById(10L)).thenReturn(Optional.of(testComplaint));
        when(upvoteRepository.existsByComplaintIdAndStudentId(10L, 1L)).thenReturn(false);

        complaintService.upvoteComplaint(10L, student);

        verify(upvoteRepository, times(1)).save(any(ComplaintUpvote.class));
        verify(complaintRepository, times(1)).incrementUpvoteCount(10L);
    }
}
