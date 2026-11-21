package com.hostel.complaint.repository;

import com.hostel.complaint.model.Category;
import com.hostel.complaint.model.Complaint;
import com.hostel.complaint.model.ComplaintStatus;
import com.hostel.complaint.model.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:block IS NULL OR c.hostelBlock = :block) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:priority IS NULL OR c.priority = :priority)")
    Page<Complaint> findFilteredComplaints(
            @Param("block") String block,
            @Param("category") Category category,
            @Param("status") ComplaintStatus status,
            @Param("priority") Priority priority,
            Pageable pageable
    );

    Page<Complaint> findByCreatedById(Long studentId, Pageable pageable);

    Page<Complaint> findByAssignedToIdAndStatusIn(Long staffId, Collection<ComplaintStatus> statuses, Pageable pageable);

    @Query("SELECT c FROM Complaint c WHERE c.status IN :statuses AND c.lastStatusChangeAt <= :threshold")
    List<Complaint> findStaleComplaints(
            @Param("statuses") Collection<ComplaintStatus> statuses,
            @Param("threshold") LocalDateTime threshold
    );

    @Modifying
    @Query("UPDATE Complaint c SET c.upvoteCount = c.upvoteCount + 1 WHERE c.id = :complaintId")
    void incrementUpvoteCount(@Param("complaintId") Long complaintId);

    @Query("SELECT c.category AS category, COUNT(c) AS count FROM Complaint c GROUP BY c.category")
    List<Map<String, Object>> countByCategory();

    @Query("SELECT c.hostelBlock AS block, COUNT(c) AS count FROM Complaint c GROUP BY c.hostelBlock")
    List<Map<String, Object>> countByHostelBlock();

    @Query("SELECT c.status AS status, COUNT(c) AS count FROM Complaint c GROUP BY c.status")
    List<Map<String, Object>> countByStatus();

    long countByStatus(ComplaintStatus status);
}
