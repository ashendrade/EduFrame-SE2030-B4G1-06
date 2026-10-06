package com.eduframepackage.eduframe.service;

import com.eduframepackage.eduframe.model.Video;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Service component responsible for managing guest and home page educational videos in EduFrame.
 * Provides thread-safe video retrieval, publishing, deletion, and keyword filtering.
 */
@Service
public class VideoService {

    private final List<Video> videos = new CopyOnWriteArrayList<>();

    /**
     * Initializes VideoService with standard pre-populated sample lectures for guest visitors.
     */
    public VideoService() {
        // Pre-populate initial guest & home page videos
        videos.add(new Video(
            "se2030-l1",
            "SE2030: Software Engineering - Introduction to MVC Architecture",
            "A comprehensive lecture explaining the Model-View-Controller design pattern in modern web architectures with concrete Java examples.",
            "Prof. Kanishka Jayasinghe",
            "45:12",
            "Computing",
            "2026-08-10",
            124,
            "/images/thumb-mvc.jpg",
            "https://www.youtube.com/embed/pTb0U4xW6h8"
        ));
        videos.add(new Video(
            "se2030-l2",
            "SE2030: Design Patterns - Singleton & Factory Patterns",
            "Deep dive into creational design patterns. Learn how to write thread-safe singletons and compile-time decoupling using factories.",
            "Prof. Kanishka Jayasinghe",
            "52:30",
            "Computing",
            "2026-08-12",
            89,
            "/images/thumb-patterns.jpg",
            "https://www.youtube.com/embed/v9ejT8FO-7I"
        ));
        videos.add(new Video(
            "it1010-l5",
            "IT1010: Object Oriented Programming in Java - Inheritance & Polymorphism",
            "In this session we cover base and subclass relationships, overriding, overloading, dynamic binding, and interface implementation.",
            "Dr. Tharindu Senanayake",
            "38:15",
            "Computing",
            "2026-08-05",
            345,
            "/images/thumb-oop.jpg",
            "https://www.youtube.com/embed/3W983z2697g"
        ));
        videos.add(new Video(
            "ee1020-l1",
            "EE1020: Digital Logic Design - Boolean Algebra & K-Maps",
            "Simplifying Boolean expressions using laws and theorems. Graphical minimization of logic circuits using Karnaugh Maps.",
            "Dr. Priyantha Alwis",
            "1:05:40",
            "Engineering",
            "2026-08-01",
            78,
            "/images/thumb-kmaps.jpg",
            "https://www.youtube.com/embed/RO5alU6CMwE"
        ));
        videos.add(new Video(
            "bm1010-l3",
            "BM1010: Principles of Marketing - Market Segmentation",
            "Analyzing how businesses divide broad target markets into consumer subsets based on demographic, geographic, and psychographic characteristics.",
            "Ms. Sanduni Perera",
            "28:40",
            "Business",
            "2026-08-09",
            156,
            "/images/thumb-marketing.jpg",
            "https://www.youtube.com/embed/hJ814j2o4Q4"
        ));
        videos.add(new Video(
            "cs3020-l4",
            "CS3020: Operating Systems - CPU Scheduling Algorithms",
            "An analytical review of CPU scheduling mechanisms including First-Come-First-Served, Shortest-Job-First, Round Robin, and Priority Scheduling.",
            "Dr. Tharindu Senanayake",
            "58:20",
            "Computing",
            "2026-08-08",
            210,
            "/images/thumb-os.jpg",
            "https://www.youtube.com/embed/EWkfqT_8e0k"
        ));
    }

    /**
     * Retrieves a list of all currently published public videos.
     *
     * @return List of Video entities.
     */
    public List<Video> getAllVideos() {
        return new ArrayList<>(videos);
    }

    /**
     * Retrieves top featured videos for display in the home page hero section.
     *
     * @return Sublist of up to 3 featured videos.
     */
    public List<Video> getFeaturedVideos() {
        if (videos.isEmpty()) {
            return new ArrayList<>();
        }
        return videos.subList(0, Math.min(3, videos.size()));
    }

    /**
     * Finds a video entity by its unique ID.
     *
     * @param id Unique video identifier.
     * @return Optional containing the Video if found, or empty Optional.
     */
    public Optional<Video> getVideoById(String id) {
        return videos.stream()
                .filter(v -> v.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    /**
     * Adds a newly published video from an instructor into the guest repository.
     * Sets automatic default values for missing ID, upload date, views, and thumbnail.
     *
     * @param video Video entity to create.
     * @return Created Video object.
     */
    public Video addVideo(Video video) {
        if (video.getId() == null || video.getId().trim().isEmpty()) {
            video.setId("vid-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (video.getUploadDate() == null || video.getUploadDate().trim().isEmpty()) {
            video.setUploadDate(LocalDate.now().toString());
        }
        if (video.getViews() == 0) {
            video.setViews(1);
        }
        if (video.getThumbnailUrl() == null || video.getThumbnailUrl().trim().isEmpty()) {
            video.setThumbnailUrl("/images/thumb-default.jpg");
        }
        // Insert new video at the front so it appears top/recent for guests on home page
        videos.add(0, video);
        return video;
    }

    /**
     * Removes a video entity by ID.
     *
     * @param id Unique video identifier.
     * @return True if a video was removed, false otherwise.
     */
    public boolean deleteVideo(String id) {
        return videos.removeIf(v -> v.getId().equalsIgnoreCase(id));
    }

    /**
     * Filters videos by search terms (title, description, lecturer) and faculty category.
     *
     * @param query Search query text.
     * @param category Faculty/category name filter.
     * @return Filtered list of videos matching criteria.
     */
    public List<Video> searchVideos(String query, String category) {
        return videos.stream()
                .filter(v -> {
                    boolean matchesCat = category == null || category.isEmpty() || category.equalsIgnoreCase("All")
                            || v.getCategory().equalsIgnoreCase(category);
                    boolean matchesQuery = query == null || query.isEmpty()
                            || v.getTitle().toLowerCase().contains(query.toLowerCase())
                            || v.getDescription().toLowerCase().contains(query.toLowerCase())
                            || v.getLecturer().toLowerCase().contains(query.toLowerCase());
                    return matchesCat && matchesQuery;
                })
                .collect(Collectors.toList());
    }
}
