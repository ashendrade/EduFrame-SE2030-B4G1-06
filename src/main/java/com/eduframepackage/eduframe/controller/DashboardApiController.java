package com.eduframepackage.eduframe.controller;

import com.eduframepackage.eduframe.dto.DashboardStatsDTO;
import com.eduframepackage.eduframe.model.PostType;
import com.eduframepackage.eduframe.quiz.repository.QuizRepository;
import com.eduframepackage.eduframe.quiz.repository.QuizUserRepository;
import com.eduframepackage.eduframe.repository.AnnouncementRepository;
import com.eduframepackage.eduframe.repository.CourseRepository;
import com.eduframepackage.eduframe.repository.TicketRepository;
import com.eduframepackage.eduframe.repository.UserRepository;
import com.eduframepackage.eduframe.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardApiController {

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private QuizUserRepository quizUserRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private CourseService courseService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        long userCount = 0;
        if (userRepository != null) {
            userCount += userRepository.count();
        }
        if (quizUserRepository != null) {
            userCount += quizUserRepository.count();
        }
        if (userCount == 0) {
            userCount = 1240; // Default baseline if database table empty
        }

        long announcementsCount = announcementRepository.findAll().stream()
                .filter(a -> a.getType() == PostType.ANNOUNCEMENT)
                .count();

        long eventsCount = announcementRepository.findAll().stream()
                .filter(a -> a.getType() == PostType.EVENT)
                .count();

        long openTickets = ticketRepository.countByStatus("Open");
        long inProgressTickets = ticketRepository.countByStatus("In Progress");
        long pendingTickets = openTickets + inProgressTickets;

        long coursesCount = courseRepository.count();
        long quizzesCount = quizRepository.count();
        long totalEnrolled = courseService.getTotalEnrolledStudentsCount();

        DashboardStatsDTO stats = new DashboardStatsDTO(
                userCount,
                announcementsCount,
                eventsCount,
                pendingTickets,
                coursesCount > 0 ? coursesCount : 5,
                coursesCount > 0 ? coursesCount : 3,
                quizzesCount,
                12,
                totalEnrolled
        );

        return ResponseEntity.ok(stats);
    }
}
