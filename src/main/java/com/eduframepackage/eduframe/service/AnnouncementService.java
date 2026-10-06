package com.eduframepackage.eduframe.service;

import com.eduframepackage.eduframe.dto.AnnouncementDTO;
import com.eduframepackage.eduframe.dto.ConflictCheckDTO;
import com.eduframepackage.eduframe.model.Announcement;
import com.eduframepackage.eduframe.model.PostStatus;
import com.eduframepackage.eduframe.model.PostType;
import com.eduframepackage.eduframe.model.UserRole;
import com.eduframepackage.eduframe.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing core business logic for Announcements and Events.
 * Handles validation, role-based authorization, schedule conflict checking,
 * auto-expiry lifecycle, and notifications.
 */
import jakarta.annotation.PostConstruct;

@Service
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository repository;
    private final NotificationService notificationService;

    @Autowired
    public AnnouncementService(AnnouncementRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void seedInitialEvents() {
        if (repository.count() == 0) {
            // Seed sample live events & announcements
            Announcement evt1 = new Announcement();
            evt1.setType(PostType.EVENT);
            evt1.setTitle("Software Architecture & Design Review");
            evt1.setContent("Interactive live session reviewing MVC architecture patterns, UML component diagrams, and Spring Boot service structure for SE2030 assignment.");
            evt1.setAuthorId("Prof. Kanishka Jayasinghe");
            evt1.setCourseId("SE2030");
            evt1.setEventDate(LocalDate.of(2026, 10, 14));
            evt1.setStartTime(LocalTime.of(14, 30));
            evt1.setEndTime(LocalTime.of(16, 30));
            evt1.setLocationUrl("https://meet.google.com/eduframe-se2030");
            evt1.setStatus(PostStatus.ACTIVE);
            repository.save(evt1);

            Announcement evt2 = new Announcement();
            evt2.setType(PostType.EVENT);
            evt2.setTitle("Mid-Term Exam Preparation & Revision");
            evt2.setContent("Comprehensive Q&A session covering OOP principles, inheritance, abstract classes, and exception handling in Java.");
            evt2.setAuthorId("Dr. Tharindu Senanayake");
            evt2.setCourseId("IT1010");
            evt2.setEventDate(LocalDate.of(2026, 10, 20));
            evt2.setStartTime(LocalTime.of(10, 0));
            evt2.setEndTime(LocalTime.of(12, 0));
            evt2.setLocationUrl("https://zoom.us/j/eduframe-it1010");
            evt2.setStatus(PostStatus.ACTIVE);
            repository.save(evt2);

            Announcement evt3 = new Announcement();
            evt3.setType(PostType.EVENT);
            evt3.setTitle("Digital Logic & K-Map Workshop");
            evt3.setContent("Hands-on lab tutorial for simplifying multi-variable boolean expressions using Karnaugh maps.");
            evt3.setAuthorId("Dr. Priyantha Alwis");
            evt3.setCourseId("EE1020");
            evt3.setEventDate(LocalDate.of(2026, 10, 27));
            evt3.setStartTime(LocalTime.of(9, 30));
            evt3.setEndTime(LocalTime.of(11, 30));
            evt3.setLocationUrl("https://teams.microsoft.com/eduframe-ee1020");
            evt3.setStatus(PostStatus.ACTIVE);
            repository.save(evt3);

            Announcement ann1 = new Announcement();
            ann1.setType(PostType.ANNOUNCEMENT);
            ann1.setTitle("Welcome to Semester 1 Academic Year 2026");
            ann1.setContent("All course materials, syllabus documents, and assignment schedules have been published across the EduFrame portal.");
            ann1.setAuthorId("Admin Team");
            ann1.setCourseId("SE2030");
            ann1.setStatus(PostStatus.ACTIVE);
            repository.save(ann1);
        }
    }

    /**
     * Creates a new announcement or event post with default ADMIN authorization.
     *
     * @param dto Announcement payload details.
     * @return Created AnnouncementDTO.
     */
    public AnnouncementDTO createPost(AnnouncementDTO dto) {
        return createPost(dto, UserRole.ADMIN);
    }

    /**
     * Creates a new announcement or event post checking user role permissions and schedule conflicts.
     *
     * @param dto Announcement payload details.
     * @param role Role of the user requesting creation.
     * @return Created AnnouncementDTO.
     */
    public AnnouncementDTO createPost(AnnouncementDTO dto, UserRole role) {
        if (role == null) role = UserRole.ADMIN;
        validateRolePermissions(role, dto.getType(), "CREATE");
        validateBasicFields(dto);

        if (dto.getType() == PostType.EVENT) {
            validateEventScheduleFields(dto.getEventDate(), dto.getStartTime(), dto.getEndTime());
            ConflictCheckDTO conflictCheck = checkScheduleConflict(
                    dto.getCourseId(),
                    dto.getEventDate(),
                    dto.getStartTime(),
                    dto.getEndTime(),
                    null
            );
            if (conflictCheck.isConflict()) {
                throw new IllegalStateException(conflictCheck.getMessage());
            }
        }

        Announcement entity = new Announcement();
        entity.setType(dto.getType() != null ? dto.getType() : PostType.ANNOUNCEMENT);
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setAuthorId(dto.getAuthorId());
        entity.setCourseId(dto.getCourseId());
        entity.setStatus(PostStatus.ACTIVE);

        if (dto.getType() == PostType.EVENT) {
            entity.setEventDate(dto.getEventDate());
            entity.setStartTime(dto.getStartTime());
            entity.setEndTime(dto.getEndTime());
            entity.setLocationUrl(dto.getLocationUrl());
        }

        Announcement saved = repository.save(entity);

        // Broadcast notification alert
        notificationService.dispatchPostNotification(saved);

        return convertToDTO(saved);
    }

    /**
     * Checks whether a proposed event schedule overlaps with existing live sessions.
     *
     * @param courseId Course ID.
     * @param date Event date.
     * @param startTime Event start time.
     * @param endTime Event end time.
     * @param excludeId Optional ID of event to exclude during update checks.
     * @return ConflictCheckDTO containing conflict status and details.
     */
    @Transactional(readOnly = true)
    public ConflictCheckDTO checkScheduleConflict(String courseId, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeId) {
        if (courseId == null || courseId.trim().isEmpty() || date == null || startTime == null || endTime == null) {
            return new ConflictCheckDTO(false, "Incomplete details for schedule check", new ArrayList<>());
        }

        if (!startTime.isBefore(endTime)) {
            return new ConflictCheckDTO(true, "Event start time must be strictly before end time.", new ArrayList<>());
        }

        List<Announcement> conflicts;
        if (excludeId != null && excludeId > 0) {
            conflicts = repository.findConflictingEventsExcludingId(courseId, date, startTime, endTime, excludeId);
        } else {
            conflicts = repository.findConflictingEvents(courseId, date, startTime, endTime);
        }

        if (!conflicts.isEmpty()) {
            List<AnnouncementDTO> conflictDTOs = conflicts.stream().map(this::convertToDTO).collect(Collectors.toList());
            String msg = String.format("Schedule Conflict Detected! %d overlapping live session(s) exist for course '%s' on %s.",
                    conflicts.size(), courseId, date);
            return new ConflictCheckDTO(true, msg, conflictDTOs);
        }

        return new ConflictCheckDTO(false, "No schedule conflict detected. Slot is available.", new ArrayList<>());
    }

    /**
     * Retrieves all posts matching optional filters (courseId, post type, status) and handles auto-expiry.
     *
     * @param courseId Filter by course ID.
     * @param type Filter by PostType (ANNOUNCEMENT / EVENT).
     * @param status Filter by PostStatus.
     * @return Filtered list of AnnouncementDTOs.
     */
    public List<AnnouncementDTO> getAllPosts(String courseId, PostType type, PostStatus status) {
        List<Announcement> posts = repository.findAll();
        
        // Auto-expiry evaluation for events
        LocalDateTime now = LocalDateTime.now();
        for (Announcement post : posts) {
            if (post.getType() == PostType.EVENT && post.getStatus() == PostStatus.ACTIVE) {
                if (post.getEventDate() != null && post.getEndTime() != null) {
                    LocalDateTime eventEndDateTime = LocalDateTime.of(post.getEventDate(), post.getEndTime());
                    if (eventEndDateTime.isBefore(now)) {
                        post.setStatus(PostStatus.EXPIRED);
                        repository.save(post);
                    }
                }
            }
        }

        return posts.stream()
                .filter(p -> (courseId == null || courseId.isEmpty() || p.getCourseId().equalsIgnoreCase(courseId)))
                .filter(p -> (type == null || p.getType() == type))
                .filter(p -> (status == null || p.getStatus() == status))
                .map(this::convertToDTO)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single post by ID.
     *
     * @param id Post ID.
     * @return AnnouncementDTO entity details.
     */
    public AnnouncementDTO getPostById(Long id) {
        Announcement entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + id));
        return convertToDTO(entity);
    }

    /**
     * Updates an existing post with default ADMIN authorization.
     *
     * @param id Post ID.
     * @param dto Updated data payload.
     * @return Updated AnnouncementDTO.
     */
    public AnnouncementDTO updatePost(Long id, AnnouncementDTO dto) {
        return updatePost(id, dto, UserRole.ADMIN);
    }

    /**
     * Updates an existing post checking user role permissions and schedule conflicts.
     *
     * @param id Post ID.
     * @param dto Updated data payload.
     * @param role UserRole executing update.
     * @return Updated AnnouncementDTO.
     */
    public AnnouncementDTO updatePost(Long id, AnnouncementDTO dto, UserRole role) {
        if (role == null) role = UserRole.ADMIN;
        Announcement existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + id));

        PostType targetType = dto.getType() != null ? dto.getType() : existing.getType();
        validateRolePermissions(role, targetType, "EDIT");
        validateBasicFields(dto);

        if (existing.getType() == PostType.EVENT || dto.getType() == PostType.EVENT) {
            validateEventScheduleFields(dto.getEventDate(), dto.getStartTime(), dto.getEndTime());
            ConflictCheckDTO conflictCheck = checkScheduleConflict(
                    dto.getCourseId(),
                    dto.getEventDate(),
                    dto.getStartTime(),
                    dto.getEndTime(),
                    id
            );
            if (conflictCheck.isConflict()) {
                throw new IllegalStateException(conflictCheck.getMessage());
            }
        }

        existing.setTitle(dto.getTitle());
        existing.setContent(dto.getContent());
        existing.setCourseId(dto.getCourseId());

        if (dto.getType() != null) {
            existing.setType(dto.getType());
        }

        if (existing.getType() == PostType.EVENT) {
            existing.setEventDate(dto.getEventDate());
            existing.setStartTime(dto.getStartTime());
            existing.setEndTime(dto.getEndTime());
            existing.setLocationUrl(dto.getLocationUrl());
        }

        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        Announcement updated = repository.save(existing);
        return convertToDTO(updated);
    }

    /**
     * Cancels or soft-deletes a post by ID.
     *
     * @param id Post ID.
     * @return Updated AnnouncementDTO with CANCELLED status.
     */
    public AnnouncementDTO cancelOrDeletePost(Long id) {
        return cancelOrDeletePost(id, UserRole.ADMIN);
    }

    /**
     * Cancels or soft-deletes a post by ID checking role permissions.
     *
     * @param id Post ID.
     * @param role UserRole executing action.
     * @return Updated AnnouncementDTO with CANCELLED status.
     */
    public AnnouncementDTO cancelOrDeletePost(Long id, UserRole role) {
        if (role == null) role = UserRole.ADMIN;
        Announcement existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + id));

        validateRolePermissions(role, existing.getType(), "DELETE");

        existing.setStatus(PostStatus.CANCELLED);
        Announcement saved = repository.save(existing);
        return convertToDTO(saved);
    }

    /**
     * Enforces Role-Based Access Control Rules:
     * - ADMIN: Full access (create, edit, read, delete announcements & events)
     * - TEACHER: Full access to announcements (create, edit, read, delete). Read ONLY for events.
     * - STUDENT: Read ONLY for both announcements and events.
     */
    private void validateRolePermissions(UserRole role, PostType postType, String action) {
        if (role == UserRole.STUDENT) {
            throw new SecurityException("Access Denied: Students have read-only access and cannot " + action.toLowerCase() + " posts.");
        }
        if ((role == UserRole.TEACHER || role == UserRole.LECTURER) && postType == PostType.EVENT) {
            throw new SecurityException("Access Denied: Lecturers/Teachers can manage announcements, but only Administrators can " + action.toLowerCase() + " events.");
        }
    }

    private void validateBasicFields(AnnouncementDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title field cannot be empty.");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Description content cannot be empty.");
        }
        if (dto.getCourseId() == null || dto.getCourseId().trim().isEmpty()) {
            throw new IllegalArgumentException("Target Course / Audience ID cannot be empty.");
        }
        if (dto.getAuthorId() == null || dto.getAuthorId().trim().isEmpty()) {
            dto.setAuthorId("Lecturer/Admin");
        }
    }

    private void validateEventScheduleFields(LocalDate date, LocalTime start, LocalTime end) {
        if (date == null) {
            throw new IllegalArgumentException("Event Date is required for scheduling an event.");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start Time and End Time are required for scheduling an event.");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Event Start Time must be strictly before End Time.");
        }
    }

    private AnnouncementDTO convertToDTO(Announcement entity) {
        return new AnnouncementDTO(
                entity.getId(),
                entity.getType(),
                entity.getTitle(),
                entity.getContent(),
                entity.getAuthorId(),
                entity.getCourseId(),
                entity.getEventDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getLocationUrl(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
