package com.eduframepackage.eduframe.model;

import jakarta.persistence.*;

@Entity
@Table(name = "course_module_content")
public class CourseModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 200)
    private String moduleTitle;

    @Column(length = 2000)
    private String summary;

    @Column(length = 500)
    private String videoUrl;

    @Column(length = 50)
    private String duration;

    @Column(length = 500)
    private String notesUrl;

    private int sequenceOrder = 1;

    public CourseModule() {
    }

    public CourseModule(Long courseId, String moduleTitle, String summary, String videoUrl, String duration, String notesUrl, int sequenceOrder) {
        this.courseId = courseId;
        this.moduleTitle = moduleTitle;
        this.summary = summary;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.notesUrl = notesUrl;
        this.sequenceOrder = sequenceOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getModuleTitle() {
        return moduleTitle;
    }

    public void setModuleTitle(String moduleTitle) {
        this.moduleTitle = moduleTitle;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNotesUrl() {
        return notesUrl;
    }

    public void setNotesUrl(String notesUrl) {
        this.notesUrl = notesUrl;
    }

    public int getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(int sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }
}
