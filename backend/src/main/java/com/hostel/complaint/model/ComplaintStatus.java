package com.hostel.complaint.model;

public enum ComplaintStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED,
    REOPENED;

    public boolean isTerminal() {
        return this == CLOSED || this == CANCELLED;
    }

    public boolean isActionable() {
        return this == OPEN || this == ASSIGNED || this == IN_PROGRESS || this == REOPENED;
    }
}
