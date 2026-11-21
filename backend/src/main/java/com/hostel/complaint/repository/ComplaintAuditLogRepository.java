package com.hostel.complaint.repository;

import com.hostel.complaint.model.ComplaintAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintAuditLogRepository extends JpaRepository<ComplaintAuditLog, Long> {
    List<ComplaintAuditLog> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);
}
