package com.hostel.complaint.service;

import com.hostel.complaint.dto.WeeklyAnalyticsResponse;
import com.hostel.complaint.model.Complaint;
import com.hostel.complaint.model.ComplaintStatus;
import com.hostel.complaint.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsService {

    private final ComplaintRepository complaintRepository;
    private final int staleHours;

    public AnalyticsService(ComplaintRepository complaintRepository,
                            @Value("${app.escalation.stale-hours:48}") int staleHours) {
        this.complaintRepository = complaintRepository;
        this.staleHours = staleHours;
    }

    @Transactional(readOnly = true)
    public WeeklyAnalyticsResponse getWeeklySummary() {
        WeeklyAnalyticsResponse res = new WeeklyAnalyticsResponse();

        long total = complaintRepository.count();
        long open = complaintRepository.countByStatus(ComplaintStatus.OPEN);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);

        res.setTotalComplaints(total);
        res.setOpenComplaints(open);
        res.setInProgressComplaints(inProgress);
        res.setResolvedComplaints(resolved);

        // Calculate SLA Breaches (>48h untouched)
        LocalDateTime threshold = LocalDateTime.now().minusHours(staleHours);
        List<Complaint> breaches = complaintRepository.findStaleComplaints(
                List.of(ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED), threshold
        );
        res.setSlaBreaches(breaches.size());

        // Calculate Average Resolution Hours
        List<Complaint> allComplaints = complaintRepository.findAll();
        double avgHours = allComplaints.stream()
                .filter(c -> c.getStatus() == ComplaintStatus.RESOLVED && c.getResolvedAt() != null)
                .mapToLong(c -> Duration.between(c.getCreatedAt(), c.getResolvedAt()).toHours())
                .average()
                .orElse(0.0);

        res.setAverageResolutionHours(Math.round(avgHours * 100.0) / 100.0);

        // Group distributions
        res.setCategoryDistribution(complaintRepository.countByCategory());
        res.setBlockDistribution(complaintRepository.countByHostelBlock());

        return res;
    }
}
