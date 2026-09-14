package com.eduframepackage.eduframe.dto;

/**
 * <h1>UC-05 Submit Support Ticket - Status Update DTO</h1>
 * 
 * <p>
 * Transfer object carrying status change and staff resolution response data
 * for existing support tickets.
 * </p>
 * 
 * @author EduFrame Group B4G1-06 (SE2030)
 * @version 1.0
 */
public class TicketStatusUpdateDTO {

    private String status;
    private String adminResponse;
    private String priority;
    private String assignedTo;

    public TicketStatusUpdateDTO() {
    }

    public TicketStatusUpdateDTO(String status, String adminResponse) {
        this.status = status;
        this.adminResponse = adminResponse;
    }

    public TicketStatusUpdateDTO(String status, String adminResponse, String priority, String assignedTo) {
        this.status = status;
        this.adminResponse = adminResponse;
        this.priority = priority;
        this.assignedTo = assignedTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    @Override
    public String toString() {
        return "TicketStatusUpdateDTO{" +
                "status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", assignedTo='" + assignedTo + '\'' +
                '}';
    }
}
