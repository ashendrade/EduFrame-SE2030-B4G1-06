package com.eduframepackage.eduframe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * <h1>UC-05 Submit Support Ticket - Ticket Domain Entity</h1>
 * 
 * <p>
 * This JPA Entity models the core domain object for the UC-05 "Submit Support Ticket" module 
 * within the EduFrame academic video and learning support platform.
 * </p>
 * 
 * <h2>ECE Architectural Context:</h2>
 * <ul>
 *   <li><b>Entity Layer:</b> Encapsulates support ticket state, metadata, and persistence mapping.</li>
 *   <li><b>Control Layer:</b> Processed via {@code TicketService} for validation, duplicate prevention, and ID generation.</li>
 *   <li><b>Boundary Layer:</b> Exposed through {@code TicketController} REST API and rendered on React UI.</li>
 * </ul>
 * 
 * <h2>UC-05 Key Attributes:</h2>
 * <ul>
 *   <li>{@code ticketId}: Unique formatted ticket code (e.g., TKT-1001, TKT-1002).</li>
 *   <li>{@code subject}: Concise title describing the student/user issue.</li>
 *   <li>{@code description}: Detailed explanation of the support request.</li>
 *   <li>{@code attachmentName}: Original file name of optional attached documentation.</li>
 *   <li>{@code status}: Current ticket workflow status. Defaults to "Open" upon creation.</li>
 * </ul>
 * 
 * @author EduFrame Group B4G1-06 (SE2030)
 * @version 1.0
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false, unique = true, length = 30)
    private String ticketId;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(name = "student_email", length = 150)
    private String studentEmail;

    @Column(name = "category", length = 50)
    private String category = "General Inquiry";

    @Column(name = "priority", length = 20)
    private String priority = "Medium";

    @Column(name = "attachment_name", length = 255)
    private String attachmentName;

    @Column(name = "attachment_type", length = 100)
    private String attachmentType;

    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    @Column(name = "attachment_size")
    private Long attachmentSize;

    @Column(name = "admin_response", length = 2000)
    private String adminResponse;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "Open";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Default no-argument constructor required by JPA specification.
     */
    public Ticket() {
    }

    /**
     * Parameterized constructor for initializing a new Support Ticket instance.
     * 
     * @param ticketId       the formatted unique ticket identifier (e.g. TKT-1001)
     * @param subject        the ticket subject title
     * @param description    the ticket detailed message body
     * @param attachmentName the optional file attachment name
     * @param attachmentType the optional file attachment MIME type
     */
    public Ticket(String ticketId, String subject, String description, String attachmentName, String attachmentType) {
        this.ticketId = ticketId;
        this.subject = subject;
        this.description = description;
        this.attachmentName = attachmentName;
        this.attachmentType = attachmentType;
        this.status = "Open";
        this.category = "General Inquiry";
        this.priority = "Medium";
    }

    /**
     * Comprehensive constructor for initializing a Support Ticket with student metadata.
     * 
     * @param ticketId       the formatted unique ticket identifier
     * @param subject        the ticket subject title
     * @param description    the ticket detailed message body
     * @param studentId      the student/user ID
     * @param studentEmail   the contact email
     * @param category       the ticket category (e.g., Technical, Video, Academic)
     * @param priority       the priority level (Low, Medium, High, Urgent)
     * @param attachmentName the optional file attachment name
     * @param attachmentType the optional file attachment MIME type
     * @param attachmentPath the stored server path
     * @param attachmentSize the file size in bytes
     */
    public Ticket(String ticketId, String subject, String description, String studentId, String studentEmail,
                  String category, String priority, String attachmentName, String attachmentType,
                  String attachmentPath, Long attachmentSize) {
        this.ticketId = ticketId;
        this.subject = subject;
        this.description = description;
        this.studentId = studentId;
        this.studentEmail = studentEmail;
        this.category = (category != null && !category.trim().isEmpty()) ? category.trim() : "General Inquiry";
        this.priority = (priority != null && !priority.trim().isEmpty()) ? priority.trim() : "Medium";
        this.attachmentName = attachmentName;
        this.attachmentType = attachmentType;
        this.attachmentPath = attachmentPath;
        this.attachmentSize = attachmentSize;
        this.status = "Open";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null || this.status.trim().isEmpty()) {
            this.status = "Open";
        }
        if (this.category == null || this.category.trim().isEmpty()) {
            this.category = "General Inquiry";
        }
        if (this.priority == null || this.priority.trim().isEmpty()) {
            this.priority = "Medium";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public Long getAttachmentSize() {
        return attachmentSize;
    }

    public void setAttachmentSize(Long attachmentSize) {
        this.attachmentSize = attachmentSize;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", ticketId='" + ticketId + '\'' +
                ", subject='" + subject + '\'' +
                ", category='" + category + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                ", studentId='" + studentId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
