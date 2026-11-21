package com.hostel.complaint.dto;

import com.hostel.complaint.model.Category;
import com.hostel.complaint.model.Complaint;
import com.hostel.complaint.model.ComplaintStatus;
import com.hostel.complaint.model.Priority;

import java.time.LocalDateTime;

public class ComplaintResponse {
    private Long id;
    private String title;
    private String description;
    private Category category;
    private String hostelBlock;
    private String roomOrArea;
    private Priority priority;
    private ComplaintStatus status;
    private String photoUrl;
    private String resolutionNotes;
    private String resolutionPhotoUrl;
    private Long createdById;
    private String createdByName;
    private String createdByEmail;
    private Long assignedToId;
    private String assignedToName;
    private String assignedToSpecialty;
    private int upvoteCount;
    private boolean hasUpvoted;
    private Long version;
    private LocalDateTime lastStatusChangeAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ComplaintResponse() {}

    public static ComplaintResponse fromEntity(Complaint complaint, boolean hasUpvoted) {
        ComplaintResponse dto = new ComplaintResponse();
        dto.setId(complaint.getId());
        dto.setTitle(complaint.getTitle());
        dto.setDescription(complaint.getDescription());
        dto.setCategory(complaint.getCategory());
        dto.setHostelBlock(complaint.getHostelBlock());
        dto.setRoomOrArea(complaint.getRoomOrArea());
        dto.setPriority(complaint.getPriority());
        dto.setStatus(complaint.getStatus());
        dto.setPhotoUrl(complaint.getPhotoUrl());
        dto.setResolutionNotes(complaint.getResolutionNotes());
        dto.setResolutionPhotoUrl(complaint.getResolutionPhotoUrl());
        dto.setUpvoteCount(complaint.getUpvoteCount());
        dto.setHasUpvoted(hasUpvoted);
        dto.setVersion(complaint.getVersion());
        dto.setLastStatusChangeAt(complaint.getLastStatusChangeAt());
        dto.setResolvedAt(complaint.getResolvedAt());
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());

        if (complaint.getCreatedBy() != null) {
            dto.setCreatedById(complaint.getCreatedBy().getId());
            dto.setCreatedByName(complaint.getCreatedBy().getFullName());
            dto.setCreatedByEmail(complaint.getCreatedBy().getEmail());
        }

        if (complaint.getAssignedTo() != null) {
            dto.setAssignedToId(complaint.getAssignedTo().getId());
            dto.setAssignedToName(complaint.getAssignedTo().getFullName());
            dto.setAssignedToSpecialty(complaint.getAssignedTo().getTradeSpecialty());
        }

        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getHostelBlock() { return hostelBlock; }
    public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }

    public String getRoomOrArea() { return roomOrArea; }
    public void setRoomOrArea(String roomOrArea) { this.roomOrArea = roomOrArea; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getResolutionPhotoUrl() { return resolutionPhotoUrl; }
    public void setResolutionPhotoUrl(String resolutionPhotoUrl) { this.resolutionPhotoUrl = resolutionPhotoUrl; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public String getCreatedByEmail() { return createdByEmail; }
    public void setCreatedByEmail(String createdByEmail) { this.createdByEmail = createdByEmail; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public String getAssignedToSpecialty() { return assignedToSpecialty; }
    public void setAssignedToSpecialty(String assignedToSpecialty) { this.assignedToSpecialty = assignedToSpecialty; }

    public int getUpvoteCount() { return upvoteCount; }
    public void setUpvoteCount(int upvoteCount) { this.upvoteCount = upvoteCount; }

    public boolean isHasUpvoted() { return hasUpvoted; }
    public void setHasUpvoted(boolean hasUpvoted) { this.hasUpvoted = hasUpvoted; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getLastStatusChangeAt() { return lastStatusChangeAt; }
    public void setLastStatusChangeAt(LocalDateTime lastStatusChangeAt) { this.lastStatusChangeAt = lastStatusChangeAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
