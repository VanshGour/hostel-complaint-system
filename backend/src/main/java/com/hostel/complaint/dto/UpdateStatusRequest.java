package com.hostel.complaint.dto;

import com.hostel.complaint.model.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Target status is required")
    private ComplaintStatus targetStatus;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;

    private String notes;
    private String photoUrl;

    public UpdateStatusRequest() {}

    public ComplaintStatus getTargetStatus() { return targetStatus; }
    public void setTargetStatus(ComplaintStatus targetStatus) { this.targetStatus = targetStatus; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
