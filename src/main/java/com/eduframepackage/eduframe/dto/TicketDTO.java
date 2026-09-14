package com.eduframepackage.eduframe.dto;

/**
 * <h1>UC-05 Submit Support Ticket - Request DTO</h1>
 * 
 * <p>
 * Transfer object carrying client support ticket submission fields from
 * React UI boundary to Spring Boot control service.
 * </p>
 * 
 * @author EduFrame Group B4G1-06 (SE2030)
 * @version 1.0
 */
public class TicketDTO {

    private String subject;
    private String description;
    private String studentId;
    private String studentEmail;
    private String category = "General Inquiry";
    private String priority = "Medium";

    public TicketDTO() {
    }

    public TicketDTO(String subject, String description) {
        this.subject = subject;
        this.description = description;
    }

    public TicketDTO(String subject, String description, String studentId, String studentEmail) {
        this.subject = subject;
        this.description = description;
        this.studentId = studentId;
        this.studentEmail = studentEmail;
    }

    public TicketDTO(String subject, String description, String studentId, String studentEmail, String category, String priority) {
        this.subject = subject;
        this.description = description;
        this.studentId = studentId;
        this.studentEmail = studentEmail;
        this.category = category;
        this.priority = priority;
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

    @Override
    public String toString() {
        return "TicketDTO{" +
                "subject='" + subject + '\'' +
                ", category='" + category + '\'' +
                ", priority='" + priority + '\'' +
                ", studentId='" + studentId + '\'' +
                '}';
    }
}
