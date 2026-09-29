package com.eduframepackage.eduframe.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * <h1>Announcement & Event Domain Entity</h1>
 * 
 * <p>
 * Entity representing an academic announcement or scheduled course event.
 * Assigned to: De Silva L. C. A. (IT25101841)
 * </p>
 */
@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PostType type;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @Column(name = "content", nullable = false, length = 3000)
    private String content;

    @Column(name = "author_id", nullable = false, length = 100)
    private String authorId;

    @Column(name = "course_id", nullable = false, length = 50)
    private String courseId;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "location_url", length = 500)
    private String locationUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PostStatus status = PostStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Announcement() {
    }

    public Announcement(PostType type, String title, String content, String authorId, String courseId) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.courseId = courseId;
        this.status = PostStatus.ACTIVE;
    }

    public Announcement(PostType type, String title, String content, String authorId, String courseId,
                        LocalDate eventDate, LocalTime startTime, LocalTime endTime, String locationUrl) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.courseId = courseId;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.locationUrl = locationUrl;
        this.status = PostStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PostStatus.ACTIVE;
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

    public PostType getType() {
        return type;
    }

    public void setType(PostType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getLocationUrl() {
        return locationUrl;
    }

    public void setLocationUrl(String locationUrl) {
        this.locationUrl = locationUrl;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
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
}
