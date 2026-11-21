package com.hostel.complaint.dto;

import com.hostel.complaint.model.Priority;
import jakarta.validation.constraints.NotNull;

public class AssignComplaintRequest {

    @NotNull(message = "Staff ID is required")
    private Long staffId;

    /**
     * Sent by frontend to assert the version the admin viewed before clicking Assign.
     * Prevents race condition if another admin assigned in between.
     */
    @NotNull(message = "Version is required for optimistic concurrency control")
    private Long version;

    private Priority priority;
    private String notes;

    public AssignComplaintRequest() {}

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
