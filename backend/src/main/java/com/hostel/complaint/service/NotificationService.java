package com.hostel.complaint.service;

import com.hostel.complaint.model.Complaint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;

    public NotificationService(
            @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender,
            @Value("${app.mail.enabled:false}") boolean mailEnabled) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
    }

    @Async
    public void notifyStatusChange(Complaint complaint, String notes) {
        String recipient = complaint.getCreatedBy().getEmail();
        String subject = String.format("[Hostel Portal] Complaint #%d Status Update: %s", complaint.getId(), complaint.getStatus());
        String body = String.format(
                "Hello %s,\n\n" +
                "Your complaint '%s' (%s - %s) has been updated.\n" +
                "New Status: %s\n" +
                "Priority: %s\n" +
                "Assigned Staff: %s\n" +
                "Notes: %s\n\n" +
                "Check the portal for full details.",
                complaint.getCreatedBy().getFullName(),
                complaint.getTitle(),
                complaint.getHostelBlock(),
                complaint.getRoomOrArea(),
                complaint.getStatus(),
                complaint.getPriority(),
                complaint.getAssignedTo() != null ? complaint.getAssignedTo().getFullName() : "Unassigned",
                notes != null ? notes : "No additional notes"
        );

        sendEmail(recipient, subject, body);
    }

    @Async
    public void notifyStaffAssignment(Complaint complaint) {
        if (complaint.getAssignedTo() == null) return;

        String recipient = complaint.getAssignedTo().getEmail();
        String subject = String.format("[Task Assignment] New Complaint #%d: %s", complaint.getId(), complaint.getTitle());
        String body = String.format(
                "Hello %s,\n\n" +
                "A maintenance complaint has been assigned to you:\n" +
                "Title: %s\n" +
                "Location: %s, %s\n" +
                "Priority: %s\n" +
                "Description: %s\n\n" +
                "Please open the portal to mark IN_PROGRESS when you begin work.",
                complaint.getAssignedTo().getFullName(),
                complaint.getTitle(),
                complaint.getHostelBlock(),
                complaint.getRoomOrArea(),
                complaint.getPriority(),
                complaint.getDescription()
        );

        sendEmail(recipient, subject, body);
    }

    @Async
    public void notifyEscalation(Complaint complaint) {
        log.warn("ALERT: Complaint #{} at {} {} breached 48h SLA and was escalated to URGENT!",
                complaint.getId(), complaint.getHostelBlock(), complaint.getRoomOrArea());

        // In a real deployment, this would email warden@campus.edu or invoke SMS webhook
        String subject = String.format("[SLA BREACH ALERT] Complaint #%d untouched for > 48h", complaint.getId());
        String body = String.format(
                "URGENT ATTENTION REQUIRED:\n" +
                "Complaint #%d ('%s') in %s %s has been untouched for more than 48 hours.\n" +
                "Status: %s\n" +
                "Priority auto-escalated to URGENT.",
                complaint.getId(), complaint.getTitle(), complaint.getHostelBlock(), complaint.getRoomOrArea(), complaint.getStatus()
        );

        sendEmail("warden@campus.edu", subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        if (mailEnabled && mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.info("Sent email notification to: {}", to);
            } catch (Exception ex) {
                log.error("Failed to send email to {}: {}", to, ex.getMessage());
            }
        } else {
            log.info("[MOCK EMAIL DISPATCH] To: {} | Subject: {}\nBody: {}", to, subject, body);
        }
    }
}
