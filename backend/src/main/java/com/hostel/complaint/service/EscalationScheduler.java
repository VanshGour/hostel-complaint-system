package com.hostel.complaint.service;

import com.hostel.complaint.model.Complaint;
import com.hostel.complaint.model.ComplaintStatus;
import com.hostel.complaint.model.Priority;
import com.hostel.complaint.repository.ComplaintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(EscalationScheduler.class);

    private final ComplaintRepository complaintRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final int staleHours;

    public EscalationScheduler(ComplaintRepository complaintRepository,
                               AuditLogService auditLogService,
                               NotificationService notificationService,
                               @Value("${app.escalation.stale-hours:48}") int staleHours) {
        this.complaintRepository = complaintRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.staleHours = staleHours;
    }

    /**
     * Periodic SLA evaluation engine.
     * Evaluates open or assigned complaints that have remained untouched for longer than 48 hours.
     */
    @Scheduled(cron = "${app.escalation.cron:0 0 */2 * * *}")
    @Transactional
    public void processSlaBreaches() {
        log.info("Running scheduled SLA evaluation scan for untouched complaints...");

        LocalDateTime threshold = LocalDateTime.now().minusHours(staleHours);
        List<Complaint> staleComplaints = complaintRepository.findStaleComplaints(
                List.of(ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED), threshold
        );

        if (staleComplaints.isEmpty()) {
            log.info("SLA check completed: No stale complaints found.");
            return;
        }

        log.warn("Found {} complaints violating SLA threshold (>{} hours untouched). Escalating...", 
                staleComplaints.size(), staleHours);

        for (Complaint complaint : staleComplaints) {
            escalateComplaint(complaint);
        }
    }

    private void escalateComplaint(Complaint complaint) {
        Priority previousPriority = complaint.getPriority();
        complaint.setPriority(Priority.URGENT);
        complaint.setLastStatusChangeAt(LocalDateTime.now());

        complaintRepository.save(complaint);

        auditLogService.recordLog(
                complaint,
                null, // System actor
                complaint.getStatus(),
                complaint.getStatus(),
                "ESCALATED",
                String.format("Automatic SLA Escalation: complaint untouched for > %d hours. Priority changed from %s to URGENT.",
                        staleHours, previousPriority)
        );

        notificationService.notifyEscalation(complaint);
    }
}
