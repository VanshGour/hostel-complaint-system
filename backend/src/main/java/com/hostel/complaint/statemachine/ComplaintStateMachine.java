package com.hostel.complaint.statemachine;

import com.hostel.complaint.model.Complaint;
import com.hostel.complaint.model.ComplaintStatus;
import com.hostel.complaint.model.Role;
import com.hostel.complaint.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Strict Finite State Machine guard for the Complaint Lifecycle.
 * Prevents invalid jumps (e.g., RESOLVED -> OPEN) and enforces role-based transition guards.
 */
@Component
public class ComplaintStateMachine {

    public void validateTransition(Complaint complaint, ComplaintStatus targetStatus, User actor, String notes) {
        ComplaintStatus currentStatus = complaint.getStatus();

        if (currentStatus == targetStatus) {
            throw new InvalidStateTransitionException(
                String.format("Complaint #%d is already in '%s' status.", complaint.getId(), currentStatus)
            );
        }

        if (currentStatus.isTerminal()) {
            throw new InvalidStateTransitionException(
                String.format("Complaint #%d is in terminal status '%s' and cannot be modified.", complaint.getId(), currentStatus)
            );
        }

        switch (currentStatus) {
            case OPEN -> handleFromOpen(complaint, targetStatus, actor, notes);
            case ASSIGNED -> handleFromAssigned(complaint, targetStatus, actor, notes);
            case IN_PROGRESS -> handleFromInProgress(complaint, targetStatus, actor, notes);
            case RESOLVED -> handleFromResolved(complaint, targetStatus, actor, notes);
            case REOPENED -> handleFromReopened(complaint, targetStatus, actor, notes);
            default -> throw new InvalidStateTransitionException(
                String.format("Illegal transition from '%s' to '%s'.", currentStatus, targetStatus)
            );
        }
    }

    private void handleFromOpen(Complaint complaint, ComplaintStatus targetStatus, User actor, String notes) {
        if (targetStatus == ComplaintStatus.ASSIGNED) {
            if (actor.getRole() != Role.ADMIN) {
                throw new InvalidStateTransitionException("Only an ADMIN/Warden can assign an OPEN complaint.");
            }
        } else if (targetStatus == ComplaintStatus.CANCELLED) {
            boolean isOwner = actor.getId().equals(complaint.getCreatedBy().getId());
            boolean isAdmin = actor.getRole() == Role.ADMIN;
            if (!isOwner && !isAdmin) {
                throw new InvalidStateTransitionException("Only the complaint creator or an ADMIN can cancel this complaint.");
            }
            if (notes == null || notes.trim().length() < 5) {
                throw new InvalidStateTransitionException("A cancellation reason of at least 5 characters is required.");
            }
        } else {
            throw new InvalidStateTransitionException(
                String.format("Cannot transition OPEN complaint to '%s'. Valid transitions are: ASSIGNED, CANCELLED.", targetStatus)
            );
        }
    }

    private void handleFromAssigned(Complaint complaint, ComplaintStatus targetStatus, User actor, String notes) {
        if (targetStatus == ComplaintStatus.IN_PROGRESS) {
            // Must be assigned staff or admin
            boolean isAssignee = complaint.getAssignedTo() != null && 
                                 complaint.getAssignedTo().getId().equals(actor.getId());
            boolean isAdmin = actor.getRole() == Role.ADMIN;
            if (!isAssignee && !isAdmin) {
                throw new InvalidStateTransitionException("Only the assigned staff member or an ADMIN can mark a complaint IN_PROGRESS.");
            }
        } else if (targetStatus == ComplaintStatus.ASSIGNED) { // Reassigning
            if (actor.getRole() != Role.ADMIN) {
                throw new InvalidStateTransitionException("Only an ADMIN can reassign this complaint.");
            }
        } else if (targetStatus == ComplaintStatus.CANCELLED) {
            if (actor.getRole() != Role.ADMIN) {
                throw new InvalidStateTransitionException("Only an ADMIN can cancel an assigned complaint.");
            }
        } else {
            throw new InvalidStateTransitionException(
                String.format("Cannot transition ASSIGNED complaint to '%s'. Valid transitions are: IN_PROGRESS, ASSIGNED (Reassign), CANCELLED.", targetStatus)
            );
        }
    }

    private void handleFromInProgress(Complaint complaint, ComplaintStatus targetStatus, User actor, String notes) {
        if (targetStatus == ComplaintStatus.RESOLVED) {
            boolean isAssignee = complaint.getAssignedTo() != null && 
                                 complaint.getAssignedTo().getId().equals(actor.getId());
            boolean isAdmin = actor.getRole() == Role.ADMIN;
            if (!isAssignee && !isAdmin) {
                throw new InvalidStateTransitionException("Only the assigned staff member or an ADMIN can mark a complaint RESOLVED.");
            }
            if (notes == null || notes.trim().length() < 10) {
                throw new InvalidStateTransitionException("Resolution notes of at least 10 characters are required explaining the fix.");
            }
        } else if (targetStatus == ComplaintStatus.ASSIGNED) { // Staff unable to complete -> reassign
            if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.STAFF) {
                throw new InvalidStateTransitionException("Unauthorized to move ticket back to ASSIGNED.");
            }
        } else {
            throw new InvalidStateTransitionException(
                String.format("Cannot transition IN_PROGRESS complaint to '%s'. Valid transitions are: RESOLVED, ASSIGNED.", targetStatus)
            );
        }
    }

    private void handleFromResolved(Complaint complaint, ComplaintStatus targetStatus, User actor, String notes) {
        if (targetStatus == ComplaintStatus.CLOSED) {
            boolean isOwner = actor.getId().equals(complaint.getCreatedBy().getId());
            boolean isAdmin = actor.getRole() == Role.ADMIN;
            if (!isOwner && !isAdmin) {
                throw new InvalidStateTransitionException("Only the student creator or ADMIN can accept and CLOSE the complaint.");
            }
        } else if (targetStatus == ComplaintStatus.REOPENED) {
            boolean isOwner = actor.getId().equals(complaint.getCreatedBy().getId());
            if (!isOwner) {
                throw new InvalidStateTransitionException("Only the student creator can REOPEN a resolved complaint.");
            }
            if (complaint.getResolvedAt() != null && 
                complaint.getResolvedAt().isBefore(LocalDateTime.now().minusHours(48))) {
                throw new InvalidStateTransitionException("Complaints cannot be reopened after 48 hours of resolution. Please file a new complaint.");
            }
            if (notes == null || notes.trim().length() < 10) {
                throw new InvalidStateTransitionException("A detailed reopening reason of at least 10 characters is required.");
            }
        } else {
            throw new InvalidStateTransitionException(
                String.format("Cannot transition RESOLVED complaint to '%s'. Valid transitions are: CLOSED, REOPENED.", targetStatus)
            );
        }
    }

    private void handleFromReopened(Complaint complaint, ComplaintStatus targetStatus, User actor, String notes) {
        if (targetStatus == ComplaintStatus.ASSIGNED) {
            if (actor.getRole() != Role.ADMIN) {
                throw new InvalidStateTransitionException("Only an ADMIN can reassign a REOPENED complaint.");
            }
        } else {
            throw new InvalidStateTransitionException(
                String.format("Cannot transition REOPENED complaint to '%s'. Reopened complaints must be re-ASSIGNED by an ADMIN.", targetStatus)
            );
        }
    }
}
