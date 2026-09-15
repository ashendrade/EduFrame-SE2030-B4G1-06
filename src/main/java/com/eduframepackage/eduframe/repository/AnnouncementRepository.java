package com.eduframepackage.eduframe.repository;

import com.eduframepackage.eduframe.model.Announcement;
import com.eduframepackage.eduframe.model.PostStatus;
import com.eduframepackage.eduframe.model.PostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByCourseId(String courseId);

    List<Announcement> findByTypeAndStatusOrderByCreatedAtDesc(PostType type, PostStatus status);

    List<Announcement> findByStatusOrderByCreatedAtDesc(PostStatus status);

    @Query("SELECT a FROM Announcement a WHERE a.courseId = :courseId AND a.type = com.eduframepackage.eduframe.model.PostType.EVENT AND a.status != com.eduframepackage.eduframe.model.PostStatus.CANCELLED AND a.eventDate = :eventDate AND (a.startTime < :endTime AND a.endTime > :startTime)")
    List<Announcement> findConflictingEvents(
            @Param("courseId") String courseId,
            @Param("eventDate") LocalDate eventDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT a FROM Announcement a WHERE a.courseId = :courseId AND a.type = com.eduframepackage.eduframe.model.PostType.EVENT AND a.status != com.eduframepackage.eduframe.model.PostStatus.CANCELLED AND a.eventDate = :eventDate AND (a.startTime < :endTime AND a.endTime > :startTime) AND a.id != :excludeId")
    List<Announcement> findConflictingEventsExcludingId(
            @Param("courseId") String courseId,
            @Param("eventDate") LocalDate eventDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
