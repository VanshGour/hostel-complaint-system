package com.hostel.complaint.service;

import com.hostel.complaint.dto.AuditLogResponse;
import com.hostel.complaint.model.Complaint;
import com.hostel.complaint.model.ComplaintAuditLog;
import com.hostel.complaint.model.ComplaintStatus;
import com.hostel.complaint.model.User;
import com.hostel.complaint.repository.ComplaintAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final ComplaintAuditLogRepository auditLogRepository;

    public AuditLogService(ComplaintAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void recordLog(Complaint complaint, User changedBy, ComplaintStatus fromStatus, 
                          ComplaintStatus toStatus, String action, String comment) {
        ComplaintAuditLog log = new ComplaintAuditLog(complaint, changedBy, fromStatus, toStatus, action, comment);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsForComplaint(Long complaintId) {
        return auditLogRepository.findByComplaintIdOrderByCreatedAtDesc(complaintId)
                .stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
