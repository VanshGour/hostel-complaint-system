package com.hostel.complaint.dto;

import com.hostel.complaint.model.ComplaintAuditLog;
import com.hostel.complaint.model.ComplaintStatus;

import java.time.LocalDateTime;

public class AuditLogResponse {
    private Long id;
    private Long complaintId;
    private Long changedById;
    private String changedByName;
    private ComplaintStatus fromStatus;
    private ComplaintStatus toStatus;
    private String action;
    private String comment;
    private LocalDateTime createdAt;

    public AuditLogResponse() {}

    public static AuditLogResponse fromEntity(ComplaintAuditLog log) {
        AuditLogResponse res = new AuditLogResponse();
        res.setId(log.getId());
        res.setComplaintId(log.getComplaint().getId());
        if (log.getChangedBy() != null) {
            res.setChangedById(log.getChangedBy().getId());
            res.setChangedByName(log.getChangedBy().getFullName());
        } else {
            res.setChangedByName("SYSTEM_CRON");
        }
        res.setFromStatus(log.getFromStatus());
        res.setToStatus(log.getToStatus());
        res.setAction(log.getAction());
        res.setComment(log.getComment());
        res.setCreatedAt(log.getCreatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }

    public Long getChangedById() { return changedById; }
    public void setChangedById(Long changedById) { this.changedById = changedById; }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

    public ComplaintStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(ComplaintStatus fromStatus) { this.fromStatus = fromStatus; }

    public ComplaintStatus getToStatus() { return toStatus; }
    public void setToStatus(ComplaintStatus toStatus) { this.toStatus = toStatus; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
