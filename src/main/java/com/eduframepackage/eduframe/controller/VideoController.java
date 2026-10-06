package com.eduframepackage.eduframe.controller;

import com.eduframepackage.eduframe.model.Video;
import com.eduframepackage.eduframe.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Controller managing guest and home page video actions, including REST endpoints
 * and instructor upload/delete web form handlers.
 */
@Controller
public class VideoController {

    @Autowired
    private VideoService videoService;

    // --- REST Endpoints for API/JS consumption ---

    /**
     * REST endpoint to retrieve all published guest videos.
     * 
     * @return List of all guest videos in JSON format.
     */
    @ResponseBody
    @GetMapping("/api/videos")
    public List<Video> getAllVideos() {
        return videoService.getAllVideos();
    }

    /**
     * REST endpoint to retrieve a specific video by ID.
     * 
     * @param id Unique video identifier.
     * @return ResponseEntity with Video if found, or 404 Not Found.
     */
    @ResponseBody
    @GetMapping("/api/videos/{id}")
    public ResponseEntity<Video> getVideoById(@PathVariable("id") String id) {
        return videoService.getVideoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * REST endpoint allowing programmatic publishing of videos.
     * 
     * @param video Video payload.
     * @param auth Current authenticated user.
     * @return ResponseEntity containing the created Video entity.
     */
    @ResponseBody
    @PostMapping("/api/videos")
    public ResponseEntity<Video> createVideoApi(@RequestBody Video video, Authentication auth) {
        if (auth != null && video.getLecturer() == null) {
            video.setLecturer(auth.getName());
        }
        Video created = videoService.addVideo(video);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * REST endpoint to delete a video by ID.
     * 
     * @param id Unique video identifier.
     * @return 204 No Content on success, or 404 Not Found.
     */
    @ResponseBody
    @DeleteMapping("/api/videos/{id}")
    public ResponseEntity<Void> deleteVideoApi(@PathVariable("id") String id) {
        boolean deleted = videoService.deleteVideo(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- Form-based Upload Endpoint for Instructors ---

    /**
     * Handles HTML form submission from instructors uploading videos for guest & home page display.
     * Supports both physical MultipartFile uploads and external stream URLs (e.g. YouTube).
     */
    @PostMapping("/videos/upload")
    public String handleVideoUpload(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam(value = "lecturer", required = false) String lecturer,
            @RequestParam(value = "duration", required = false) String duration,
            @RequestParam(value = "videoUrl", required = false) String videoUrlInput,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String videoUrl = videoUrlInput;

        // Process file upload if provided
        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "video.mp4";
                String fileName = System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
                Path uploadPath = Paths.get("uploads");

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                videoUrl = "/uploads/" + fileName;
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload video file: " + e.getMessage());
                return "redirect:/upload";
            }
        }

        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            videoUrl = "https://www.youtube.com/embed/pTb0U4xW6h8"; // Default instructional preview fallback
        }

        String lecturerName = lecturer;
        if (lecturerName == null || lecturerName.trim().isEmpty()) {
            lecturerName = auth != null ? auth.getName() : "Guest Lecturer";
        }

        String videoDuration = duration;
        if (videoDuration == null || videoDuration.trim().isEmpty()) {
            videoDuration = "15:00";
        }

        Video newVideo = new Video();
        newVideo.setTitle(title);
        newVideo.setDescription(description);
        newVideo.setCategory(category != null ? category : "Computing");
        newVideo.setLecturer(lecturerName);
        newVideo.setDuration(videoDuration);
        newVideo.setVideoUrl(videoUrl);
        newVideo.setThumbnailUrl("/images/thumb-default.jpg");

        videoService.addVideo(newVideo);

        redirectAttributes.addFlashAttribute("successMessage", "Guest & Home Page Video uploaded successfully! It is now visible to all guests and students on the home page.");
        return "redirect:/upload";
    }

    /**
     * Handles web form request to delete a published guest video.
     * 
     * @param id Unique identifier of the video to delete.
     * @param redirectAttributes Redirect feedback attributes.
     * @return Redirect to instructor upload management workspace.
     */
    @PostMapping("/videos/delete/{id}")
    public String handleDeleteVideo(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        boolean removed = videoService.deleteVideo(id);
        if (removed) {
            redirectAttributes.addFlashAttribute("successMessage", "Video removed from public/guest home portal.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Video not found.");
        }
        return "redirect:/upload";
    }
}
