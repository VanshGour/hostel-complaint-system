package com.hostel.complaint;

import com.hostel.complaint.model.*;
import com.hostel.complaint.statemachine.ComplaintStateMachine;
import com.hostel.complaint.statemachine.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ComplaintStateMachineTest {

    private ComplaintStateMachine stateMachine;
    private User student;
    private User admin;
    private User staff;
    private Complaint complaint;

    @BeforeEach
    void setUp() {
        stateMachine = new ComplaintStateMachine();

        student = new User("student@campus.edu", "pass", "Student One", "123", Role.STUDENT, "Block A", "304", null);
        student.setId(10L);

        admin = new User("warden@campus.edu", "pass", "Warden", "456", Role.ADMIN, "Block A", "Office", null);
        admin.setId(20L);

        staff = new User("plumber@campus.edu", "pass", "Plumber", "789", Role.STAFF, "Campus", "Maint", "PLUMBING");
        staff.setId(30L);

        complaint = new Complaint();
        complaint.setId(100L);
        complaint.setTitle("Leaking Pipe");
        complaint.setStatus(ComplaintStatus.OPEN);
        complaint.setCreatedBy(student);
        complaint.setVersion(0L);
    }

    @Test
    @DisplayName("Admin can assign OPEN complaint to Staff")
    void testAdminAssignsOpenComplaint() {
        assertDoesNotThrow(() ->
            stateMachine.validateTransition(complaint, ComplaintStatus.ASSIGNED, admin, "Assigning to plumber")
        );
    }

    @Test
    @DisplayName("Student cannot assign OPEN complaint - throws InvalidStateTransitionException")
    void testStudentCannotAssign() {
        InvalidStateTransitionException ex = assertThrows(
            InvalidStateTransitionException.class,
            () -> stateMachine.validateTransition(complaint, ComplaintStatus.ASSIGNED, student, "Attempt assign")
        );
        assertTrue(ex.getMessage().contains("Only an ADMIN/Warden can assign"));
    }

    @Test
    @DisplayName("Assigned staff can move ticket to IN_PROGRESS")
    void testStaffMovesToInProgress() {
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        complaint.setAssignedTo(staff);

        assertDoesNotThrow(() ->
            stateMachine.validateTransition(complaint, ComplaintStatus.IN_PROGRESS, staff, "Started work")
        );
    }

    @Test
    @DisplayName("Staff resolving without notes throws validation error")
    void testStaffResolvesWithoutNotesThrows() {
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setAssignedTo(staff);

        InvalidStateTransitionException ex = assertThrows(
            InvalidStateTransitionException.class,
            () -> stateMachine.validateTransition(complaint, ComplaintStatus.RESOLVED, staff, "Fixed") // < 10 chars
        );
        assertTrue(ex.getMessage().contains("Resolution notes of at least 10 characters are required"));
    }

    @Test
    @DisplayName("Direct transition from RESOLVED to OPEN is forbidden")
    void testResolvedToOpenForbidden() {
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolvedAt(LocalDateTime.now().minusHours(2));

        InvalidStateTransitionException ex = assertThrows(
            InvalidStateTransitionException.class,
            () -> stateMachine.validateTransition(complaint, ComplaintStatus.OPEN, student, "Reset")
        );
        assertTrue(ex.getMessage().contains("Cannot transition RESOLVED complaint to 'OPEN'"));
    }

    @Test
    @DisplayName("Student can REOPEN a resolved complaint within 48 hours")
    void testStudentCanReopenWithin48Hours() {
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolvedAt(LocalDateTime.now().minusHours(10));

        assertDoesNotThrow(() ->
            stateMachine.validateTransition(
                complaint, 
                ComplaintStatus.REOPENED, 
                student, 
                "The pipe is still dripping slowly from the joint"
            )
        );
    }

    @Test
    @DisplayName("Student cannot REOPEN after 48 hours has passed")
    void testStudentCannotReopenAfter48Hours() {
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolvedAt(LocalDateTime.now().minusHours(52));

        InvalidStateTransitionException ex = assertThrows(
            InvalidStateTransitionException.class,
            () -> stateMachine.validateTransition(
                complaint, 
                ComplaintStatus.REOPENED, 
                student, 
                "Problem recurred after several days"
            )
        );
        assertTrue(ex.getMessage().contains("cannot be reopened after 48 hours"));
    }
}
