package com.eduframepackage.eduframe.dto;

/**
 * DTO for database-synced Dashboard KPI Metrics
 */
public class DashboardStatsDTO {

    private long systemUsers;
    private long totalAnnouncements;
    private long scheduledEvents;
    private long pendingTickets;
    private long enrolledModules;
    private long totalCourses;
    private long totalQuizzes;
    private long lectureUploads;
    private long totalEnrolledStudents;

    public DashboardStatsDTO() {
    }

    public DashboardStatsDTO(long systemUsers, long totalAnnouncements, long scheduledEvents,
                             long pendingTickets, long enrolledModules, long totalCourses,
                             long totalQuizzes, long lectureUploads, long totalEnrolledStudents) {
        this.systemUsers = systemUsers;
        this.totalAnnouncements = totalAnnouncements;
        this.scheduledEvents = scheduledEvents;
        this.pendingTickets = pendingTickets;
        this.enrolledModules = enrolledModules;
        this.totalCourses = totalCourses;
        this.totalQuizzes = totalQuizzes;
        this.lectureUploads = lectureUploads;
        this.totalEnrolledStudents = totalEnrolledStudents;
    }

    public long getSystemUsers() {
        return systemUsers;
    }

    public void setSystemUsers(long systemUsers) {
        this.systemUsers = systemUsers;
    }

    public long getTotalAnnouncements() {
        return totalAnnouncements;
    }

    public void setTotalAnnouncements(long totalAnnouncements) {
        this.totalAnnouncements = totalAnnouncements;
    }

    public long getScheduledEvents() {
        return scheduledEvents;
    }

    public void setScheduledEvents(long scheduledEvents) {
        this.scheduledEvents = scheduledEvents;
    }

    public long getPendingTickets() {
        return pendingTickets;
    }

    public void setPendingTickets(long pendingTickets) {
        this.pendingTickets = pendingTickets;
    }

    public long getEnrolledModules() {
        return enrolledModules;
    }

    public void setEnrolledModules(long enrolledModules) {
        this.enrolledModules = enrolledModules;
    }

    public long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public long getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(long totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public long getLectureUploads() {
        return lectureUploads;
    }

    public void setLectureUploads(long lectureUploads) {
        this.lectureUploads = lectureUploads;
    }

    public long getTotalEnrolledStudents() {
        return totalEnrolledStudents;
    }

    public void setTotalEnrolledStudents(long totalEnrolledStudents) {
        this.totalEnrolledStudents = totalEnrolledStudents;
    }
}
