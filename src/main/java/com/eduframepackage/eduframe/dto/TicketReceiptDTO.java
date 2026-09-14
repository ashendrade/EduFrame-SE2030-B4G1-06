package com.eduframepackage.eduframe.dto;

import java.time.LocalDateTime;

/**
 * <h1>UC-05 Submit Support Ticket - eTicketReceipt DTO</h1>
 * 
 * <p>
 * Official digital confirmation receipt DTO returned to the frontend boundary upon
 * successful processing of a support ticket. Contains unique ticket ID, submission timestamp,
 * status ("Open"), and summary details.
 * </p>
 * 
 * @author EduFrame Group B4G1-06 (SE2030)
 * @version 1.0
 */
public class TicketReceiptDTO {

    private String ticketId;
    private String subject;
    private String description;
    private String category;
    private String priority;
    private String status;
    private String studentId;
    private String studentEmail;
    private String attachmentName;
    private Long attachmentSize;
    private String adminResponse;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String receiptMessage;

    public TicketReceiptDTO() {
    }

    public TicketReceiptDTO(String ticketId, String subject, String description, String status, String attachmentName, LocalDateTime createdAt, String receiptMessage) {
        this.ticketId = ticketId;
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.attachmentName = attachmentName;
        this.createdAt = createdAt;
        this.receiptMessage = receiptMessage;
    }

    public TicketReceiptDTO(String ticketId, String subject, String description, String category, String priority,
                            String status, String studentId, String studentEmail, String attachmentName,
                            Long attachmentSize, String adminResponse, String assignedTo,
                            LocalDateTime createdAt, LocalDateTime updatedAt, String receiptMessage) {
        this.ticketId = ticketId;
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.studentId = studentId;
        this.studentEmail = studentEmail;
        this.attachmentName = attachmentName;
        this.attachmentSize = attachmentSize;
        this.adminResponse = adminResponse;
        this.assignedTo = assignedTo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.receiptMessage = receiptMessage;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
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

    public String getReceiptMessage() {
        return receiptMessage;
    }

    public void setReceiptMessage(String receiptMessage) {
        this.receiptMessage = receiptMessage;
    }

    @Override
    public String toString() {
        return "TicketReceiptDTO{" +
                "ticketId='" + ticketId + '\'' +
                ", status='" + status + '\'' +
                ", category='" + category + '\'' +
                ", priority='" + priority + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
