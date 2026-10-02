package com.eduframepackage.eduframe.controller;

import com.eduframepackage.eduframe.dto.AnnouncementDTO;
import com.eduframepackage.eduframe.dto.ConflictCheckDTO;
import com.eduframepackage.eduframe.model.PostStatus;
import com.eduframepackage.eduframe.model.PostType;
import com.eduframepackage.eduframe.model.UserRole;
import com.eduframepackage.eduframe.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>Announcement & Event System REST Controller</h1>
 * 
 * <p>
 * REST boundary controller for creating, reading, updating, deleting notices and events,
 * and checking real-time schedule conflicts with Role-Based Access Control.
 * Assigned to: De Silva L. C. A. (IT25101841)
 * </p>
 */
@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Autowired
    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * POST /api/announcements - Publish a new Announcement or Event.
     */
    @PostMapping
    public ResponseEntity<?> createAnnouncement(
            @RequestBody AnnouncementDTO dto,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "role", required = false) UserRole roleParam) {
        try {
            UserRole userRole = parseRole(roleHeader, roleParam);
            AnnouncementDTO created = announcementService.createPost(dto, userRole);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (SecurityException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Access Denied");
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Validation Error");
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        } catch (IllegalStateException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Schedule Conflict");
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Server Error");
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    /**
     * GET /api/announcements - Retrieve announcements and events with optional filtering.
     */
    @GetMapping
    public ResponseEntity<List<AnnouncementDTO>> getAnnouncements(
            @RequestParam(value = "courseId", required = false) String courseId,
            @RequestParam(value = "type", required = false) PostType type,
            @RequestParam(value = "status", required = false) PostStatus status) {
        List<AnnouncementDTO> posts = announcementService.getAllPosts(courseId, type, status);
        return ResponseEntity.ok(posts);
    }

    /**
     * GET /api/announcements/{id} - Fetch single post by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAnnouncementById(@PathVariable("id") Long id) {
        try {
            AnnouncementDTO dto = announcementService.getPostById(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
    }

    /**
     * PUT /api/announcements/{id} - Edit announcement/event details or reschedule.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAnnouncement(
            @PathVariable("id") Long id,
            @RequestBody AnnouncementDTO dto,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "role", required = false) UserRole roleParam) {
        try {
            UserRole userRole = parseRole(roleHeader, roleParam);
            AnnouncementDTO updated = announcementService.updatePost(id, dto, userRole);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Access Denied");
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Validation Error");
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        } catch (IllegalStateException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Schedule Conflict");
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Server Error");
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    /**
     * DELETE /api/announcements/{id} - Soft delete / cancel announcement or event.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "role", required = false) UserRole roleParam) {
        try {
            UserRole userRole = parseRole(roleHeader, roleParam);
            AnnouncementDTO cancelled = announcementService.cancelOrDeletePost(id, userRole);
            return ResponseEntity.ok(cancelled);
        } catch (SecurityException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Access Denied");
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
    }

    /**
     * GET /api/announcements/conflicts - Availability query endpoint for real-time frontend conflict checking.
     */
    @GetMapping("/conflicts")
    public ResponseEntity<ConflictCheckDTO> checkConflicts(
            @RequestParam("courseId") String courseId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime end,
            @RequestParam(value = "excludeId", required = false) Long excludeId) {
        ConflictCheckDTO result = announcementService.checkScheduleConflict(courseId, date, start, end, excludeId);
        return ResponseEntity.ok(result);
    }

    private UserRole parseRole(String header, UserRole param) {
        if (param != null) return param;
        if (header != null && !header.trim().isEmpty()) {
            try {
                return UserRole.valueOf(header.trim().toUpperCase());
            } catch (Exception ignored) {
            }
        }
        return UserRole.ADMIN; // Default fallback for backward compatibility
    }
}
