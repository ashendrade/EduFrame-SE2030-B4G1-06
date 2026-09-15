package com.eduframepackage.eduframe.controller;

import com.eduframepackage.eduframe.dto.AnnouncementDTO;
import com.eduframepackage.eduframe.dto.ConflictCheckDTO;
import com.eduframepackage.eduframe.model.PostStatus;
import com.eduframepackage.eduframe.model.PostType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AnnouncementControllerTest {

    @Autowired
    private AnnouncementController announcementController;

    @Test
    @DisplayName("POST /api/announcements endpoint should return 201 CREATED")
    void testCreateAnnouncementEndpoint() {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setType(PostType.ANNOUNCEMENT);
        dto.setTitle("Test Announcement Endpoint");
        dto.setContent("Testing REST Controller response.");
        dto.setCourseId("SE2030");
        dto.setAuthorId("Prof. Kanishka");

        ResponseEntity<?> response = announcementController.createAnnouncement(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof AnnouncementDTO);
        AnnouncementDTO result = (AnnouncementDTO) response.getBody();
        assertNotNull(result.getId());
        assertEquals("Test Announcement Endpoint", result.getTitle());
    }

    @Test
    @DisplayName("GET /api/announcements endpoint should return 200 OK list")
    void testGetAnnouncementsEndpoint() {
        ResponseEntity<List<AnnouncementDTO>> response = announcementController.getAnnouncements(null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GET /api/announcements/conflicts endpoint should validate schedule slot")
    void testCheckConflictsEndpoint() {
        LocalDate date = LocalDate.now().plusDays(3);
        LocalTime start = LocalTime.of(14, 0);
        LocalTime end = LocalTime.of(16, 0);

        ResponseEntity<ConflictCheckDTO> response = announcementController.checkConflicts("SE2030", date, start, end, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isConflict());
    }

    @Test
    @DisplayName("DELETE /api/announcements/{id} endpoint should cancel post")
    void testDeleteAnnouncementEndpoint() {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setType(PostType.ANNOUNCEMENT);
        dto.setTitle("Announcement to Delete");
        dto.setContent("Will be cancelled.");
        dto.setCourseId("SE2030");

        ResponseEntity<?> createRes = announcementController.createAnnouncement(dto);
        AnnouncementDTO created = (AnnouncementDTO) createRes.getBody();

        ResponseEntity<?> deleteRes = announcementController.deleteAnnouncement(created.getId());
        assertEquals(HttpStatus.OK, deleteRes.getStatusCode());
        AnnouncementDTO cancelled = (AnnouncementDTO) deleteRes.getBody();
        assertEquals(PostStatus.CANCELLED, cancelled.getStatus());
    }
}
