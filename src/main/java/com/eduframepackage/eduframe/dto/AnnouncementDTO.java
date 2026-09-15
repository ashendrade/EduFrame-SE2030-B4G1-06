package com.eduframepackage.eduframe.dto;

import com.eduframepackage.eduframe.model.PostStatus;
import com.eduframepackage.eduframe.model.PostType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Data Transfer Object for Announcement and Event requests/responses.
 */
public class AnnouncementDTO {

    private Long id;
    private PostType type;
    private String title;
    private String content;
    private String authorId;
    private String courseId;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String locationUrl;
    private PostStatus status;
    private LocalDateTime createdAt;

    public AnnouncementDTO() {
    }

    public AnnouncementDTO(Long id, PostType type, String title, String content, String authorId, String courseId,
                           LocalDate eventDate, LocalTime startTime, LocalTime endTime, String locationUrl,
                           PostStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.courseId = courseId;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.locationUrl = locationUrl;
        this.status = status;
        this.createdAt = createdAt;
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
}
