package com.eduframepackage.eduframe.dto;

/**
 * <h1>UC-05 Submit Support Ticket - Statistics KPI DTO</h1>
 * 
 * <p>
 * Aggregates high-level metrics for the Help Desk Console: total tickets,
 * count by status (Open, In Progress, Resolved, Closed).
 * </p>
 * 
 * @author EduFrame Group B4G1-06 (SE2030)
 * @version 1.0
 */
public class TicketStatsDTO {

    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;

    public TicketStatsDTO() {
    }

    public TicketStatsDTO(long totalTickets, long openTickets, long inProgressTickets, long resolvedTickets, long closedTickets) {
        this.totalTickets = totalTickets;
        this.openTickets = openTickets;
        this.inProgressTickets = inProgressTickets;
        this.resolvedTickets = resolvedTickets;
        this.closedTickets = closedTickets;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getInProgressTickets() {
        return inProgressTickets;
    }

    public void setInProgressTickets(long inProgressTickets) {
        this.inProgressTickets = inProgressTickets;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }

    @Override
    public String toString() {
        return "TicketStatsDTO{" +
                "totalTickets=" + totalTickets +
                ", openTickets=" + openTickets +
                ", inProgressTickets=" + inProgressTickets +
                ", resolvedTickets=" + resolvedTickets +
                ", closedTickets=" + closedTickets +
                '}';
    }
}
