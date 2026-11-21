package com.hostel.complaint.repository;

import com.hostel.complaint.model.ComplaintUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintUpvoteRepository extends JpaRepository<ComplaintUpvote, Long> {
    boolean existsByComplaintIdAndStudentId(Long complaintId, Long studentId);
    List<ComplaintUpvote> findByStudentId(Long studentId);
}
