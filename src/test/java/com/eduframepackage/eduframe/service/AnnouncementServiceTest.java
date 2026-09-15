package com.eduframepackage.eduframe.service;

import com.eduframepackage.eduframe.dto.AnnouncementDTO;
import com.eduframepackage.eduframe.model.PostStatus;
import com.eduframepackage.eduframe.model.PostType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AnnouncementServiceTest {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private NotificationService notificationService;

    @Test
    @DisplayName("Should create announcement successfully and dispatch notification")
    void testCreateAnnouncement() {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setType(PostType.ANNOUNCEMENT);
        dto.setTitle("SE2030 Sprint 4 Review Guidelines");
        dto.setContent("Please review the briefing document before submitting project artifacts.");
        dto.setCourseId("SE2030");
        dto.setAuthorId("Prof. Kanishka");

        AnnouncementDTO created = announcementService.createPost(dto);

        assertNotNull(created.getId());
        assertEquals(PostType.ANNOUNCEMENT, created.getType());
        assertEquals(PostStatus.ACTIVE, created.getStatus());
        assertEquals("SE2030", created.getCourseId());

        List<String> log = notificationService.getDispatchedLog();
        assertFalse(log.isEmpty());
        assertTrue(log.stream().anyMatch(msg -> msg.contains("SE2030 Sprint 4 Review Guidelines")));
    }

    @Test
    @DisplayName("Should detect schedule conflict when creating overlapping event for same course")
    void testScheduleConflictDetection() {
        LocalDate date = LocalDate.now().plusDays(5);
        LocalTime start1 = LocalTime.of(10, 0);
        LocalTime end1 = LocalTime.of(12, 0);

        AnnouncementDTO event1 = new AnnouncementDTO();
        event1.setType(PostType.EVENT);
        event1.setTitle("SE2030 Live Workshop Session 1");
        event1.setContent("Interactive session covering domain modeling.");
        event1.setCourseId("SE2030");
        event1.setAuthorId("Prof. Kanishka");
        event1.setEventDate(date);
        event1.setStartTime(start1);
        event1.setEndTime(end1);
        event1.setLocationUrl("https://teams.microsoft.com/l/meetup/1");

        announcementService.createPost(event1);

        // Attempting to create an overlapping event: 11:00 to 13:00 on the same date and course
        AnnouncementDTO event2 = new AnnouncementDTO();
        event2.setType(PostType.EVENT);
        event2.setTitle("SE2030 Overlapping Session");
        event2.setContent("Conflicting slot.");
        event2.setCourseId("SE2030");
        event2.setAuthorId("Dr. Tharindu");
        event2.setEventDate(date);
        event2.setStartTime(LocalTime.of(11, 0));
        event2.setEndTime(LocalTime.of(13, 0));
        event2.setLocationUrl("https://teams.microsoft.com/l/meetup/2");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> announcementService.createPost(event2));
        assertTrue(ex.getMessage().contains("Schedule Conflict"));
    }

    @Test
    @DisplayName("Should allow non-overlapping event for same course")
    void testNonOverlappingEvent() {
        LocalDate date = LocalDate.now().plusDays(10);

        AnnouncementDTO event1 = new AnnouncementDTO();
        event1.setType(PostType.EVENT);
        event1.setTitle("Morning Session");
        event1.setContent("Morning details.");
        event1.setCourseId("EE1020");
        event1.setAuthorId("Prof. Kanishka");
        event1.setEventDate(date);
        event1.setStartTime(LocalTime.of(9, 0));
        event1.setEndTime(LocalTime.of(10, 30));
        announcementService.createPost(event1);

        AnnouncementDTO event2 = new AnnouncementDTO();
        event2.setType(PostType.EVENT);
        event2.setTitle("Afternoon Session");
        event2.setContent("Afternoon details.");
        event2.setCourseId("EE1020");
        event2.setAuthorId("Prof. Kanishka");
        event2.setEventDate(date);
        event2.setStartTime(LocalTime.of(11, 0));
        event2.setEndTime(LocalTime.of(12, 30));

        assertDoesNotThrow(() -> announcementService.createPost(event2));
    }

    @Test
    @DisplayName("Should cancel/soft-delete post correctly")
    void testCancelPost() {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setType(PostType.ANNOUNCEMENT);
        dto.setTitle("Post to be cancelled");
        dto.setContent("This post will be soft deleted.");
        dto.setCourseId("IT1010");
        dto.setAuthorId("Dr. Tharindu");

        AnnouncementDTO created = announcementService.createPost(dto);
        AnnouncementDTO cancelled = announcementService.cancelOrDeletePost(created.getId());

        assertEquals(PostStatus.CANCELLED, cancelled.getStatus());
    }
}
