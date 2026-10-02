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

    public AnnouncementDTO createPost(AnnouncementDTO dto) {
        return createPost(dto, UserRole.ADMIN);
    }

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

    public AnnouncementDTO getPostById(Long id) {
        Announcement entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + id));
        return convertToDTO(entity);
    }

    public AnnouncementDTO updatePost(Long id, AnnouncementDTO dto) {
        return updatePost(id, dto, UserRole.ADMIN);
    }

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

    public AnnouncementDTO cancelOrDeletePost(Long id) {
        return cancelOrDeletePost(id, UserRole.ADMIN);
    }

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
