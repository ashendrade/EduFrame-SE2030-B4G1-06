package com.eduframepackage.eduframe.controller;

import com.eduframepackage.eduframe.dto.TicketReceiptDTO;
import com.eduframepackage.eduframe.model.Video;
import com.eduframepackage.eduframe.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PageController {

    @Autowired
    private TicketService ticketService;

    private final List<Video> mockVideos = new ArrayList<>();

    public PageController() {
        // Initialize mock videos resembling courses/modules at SLIIT
        mockVideos.add(new Video(
            "se2030-l1",
            "SE2030: Software Engineering - Introduction to MVC Architecture",
            "A comprehensive lecture explaining the Model-View-Controller design pattern in modern web architectures with concrete Java examples.",
            "Prof. Kanishka Jayasinghe",
            "45:12",
            "Computing",
            "2026-08-10",
            124,
            "/images/thumb-mvc.jpg",
            "/videos/mvc.mp4"
        ));
        mockVideos.add(new Video(
            "se2030-l2",
            "SE2030: Design Patterns - Singleton & Factory Patterns",
            "Deep dive into creational design patterns. Learn how to write thread-safe singletons and compile-time decoupling using factories.",
            "Prof. Kanishka Jayasinghe",
            "52:30",
            "Computing",
            "2026-08-12",
            89,
            "/images/thumb-patterns.jpg",
            "/videos/patterns.mp4"
        ));
        mockVideos.add(new Video(
            "it1010-l5",
            "IT1010: Object Oriented Programming in Java - Inheritance & Polymorphism",
            "In this session we cover base and subclass relationships, overriding, overloading, dynamic binding, and interface implementation.",
            "Dr. Tharindu Senanayake",
            "38:15",
            "Computing",
            "2026-08-05",
            345,
            "/images/thumb-oop.jpg",
            "/videos/oop.mp4"
        ));
        mockVideos.add(new Video(
            "ee1020-l1",
            "EE1020: Digital Logic Design - Boolean Algebra & K-Maps",
            "Simplifying Boolean expressions using laws and theorems. Graphical minimization of logic circuits using Karnaugh Maps.",
            "Dr. Priyantha Alwis",
            "1:05:40",
            "Engineering",
            "2026-08-01",
            78,
            "/images/thumb-kmaps.jpg",
            "/videos/kmaps.mp4"
        ));
        mockVideos.add(new Video(
            "bm1010-l3",
            "BM1010: Principles of Marketing - Market Segmentation",
            "Analyzing how businesses divide broad target markets into consumer subsets based on demographic, geographic, and psychographic characteristics.",
            "Ms. Sanduni Perera",
            "28:40",
            "Business",
            "2026-08-09",
            156,
            "/images/thumb-marketing.jpg",
            "/videos/marketing.mp4"
        ));
        mockVideos.add(new Video(
            "cs3020-l4",
            "CS3020: Operating Systems - CPU Scheduling Algorithms",
            "An analytical review of CPU scheduling mechanisms including First-Come-First-Served, Shortest-Job-First, Round Robin, and Priority Scheduling.",
            "Dr. Tharindu Senanayake",
            "58:20",
            "Computing",
            "2026-08-08",
            210,
            "/images/thumb-os.jpg",
            "/videos/os.mp4"
        ));
    }

    @GetMapping("/")
    public String index(Model model) {
        // Pass top/recent videos to home page
        model.addAttribute("featuredVideos", mockVideos.subList(0, 3));
        model.addAttribute("recentVideos", mockVideos);
        return "index";
    }

    @GetMapping("/browse")
    public String browse(@RequestParam(value = "search", required = false) String search,
                         @RequestParam(value = "category", required = false) String category,
                         Model model) {
        List<Video> filtered = mockVideos;

        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
            filtered = filtered.stream()
                .filter(v -> v.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
        }

        if (search != null && !search.isEmpty()) {
            String q = search.toLowerCase();
            filtered = filtered.stream()
                .filter(v -> v.getTitle().toLowerCase().contains(q) || 
                             v.getDescription().toLowerCase().contains(q) || 
                             v.getLecturer().toLowerCase().contains(q))
                .collect(Collectors.toList());
        }

        model.addAttribute("videos", filtered);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedCategory", category != null ? category : "All");
        return "browse";
    }

    @GetMapping("/play/{id}")
    public String play(@PathVariable("id") String id, Model model) {
        Video currentVideo = mockVideos.stream()
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .orElse(mockVideos.get(0)); // default to first video if not found

        // Related videos from the same category (excluding current)
        List<Video> relatedVideos = mockVideos.stream()
            .filter(v -> !v.getId().equals(currentVideo.getId()) && v.getCategory().equals(currentVideo.getCategory()))
            .collect(Collectors.toList());

        // Fallback related if none in same category
        if (relatedVideos.isEmpty()) {
            relatedVideos = mockVideos.stream()
                .filter(v -> !v.getId().equals(currentVideo.getId()))
                .collect(Collectors.toList());
        }

        model.addAttribute("video", currentVideo);
        model.addAttribute("playlist", relatedVideos);
        return "play";
    }

    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/support")
    public String support() {
        return "support";
    }

    @GetMapping("/staff/helpdesk")
    public String staffHelpDesk() {
        return "staff-helpdesk";
    }

    @GetMapping({"/staff/tickets/{ticketId}", "/staff/helpdesk/review/{ticketId}"})
    public String staffTicketReview(@PathVariable("ticketId") String ticketId, Model model) {
        try {
            TicketReceiptDTO ticket = ticketService.getTicketReceipt(ticketId);
            model.addAttribute("ticket", ticket);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ticket not found with ID: " + ticketId);
        }
        model.addAttribute("ticketId", ticketId);
        return "staff-ticket-detail";
    }

    @GetMapping("/staff/login")
    public String staffLogin() {
        return "redirect:/login?role=staff";
    }
}
