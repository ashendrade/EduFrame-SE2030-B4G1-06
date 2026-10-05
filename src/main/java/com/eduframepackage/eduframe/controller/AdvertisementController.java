package com.eduframepackage.eduframe.controller;

import com.eduframepackage.eduframe.model.Advertisement;
import com.eduframepackage.eduframe.service.AdvertisementService;
import com.eduframepackage.eduframe.strategy.MediaUploadStrategy;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;
    private final List<MediaUploadStrategy> mediaUploadStrategies;

    public AdvertisementController(
            AdvertisementService advertisementService,
            List<MediaUploadStrategy> mediaUploadStrategies) {
        this.advertisementService = advertisementService;
        this.mediaUploadStrategies = mediaUploadStrategies;
    }

    // CREATE
    @PostMapping
    public Advertisement createAdvertisement(@Valid @RequestBody Advertisement advertisement) {
        return advertisementService.createAdvertisement(advertisement);
    }

    // READ ALL
    @GetMapping
    public List<Advertisement> getAllAdvertisements() {
        return advertisementService.getAllAdvertisements();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Advertisement> getAdvertisementById(@PathVariable Long id) {
        return advertisementService.getAdvertisementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public Advertisement updateAdvertisement(
            @PathVariable Long id,
            @Valid @RequestBody Advertisement advertisement) {
        return advertisementService.updateAdvertisement(id, advertisement);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvertisement(@PathVariable Long id) {
        advertisementService.deleteAdvertisement(id);
        return ResponseEntity.noContent().build();
    }

    // UPLOAD IMAGE OR VIDEO
    @PostMapping("/{id}/upload")
    public Advertisement uploadMedia(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws Exception {

        Advertisement advertisement = advertisementService
                .getAdvertisementById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        String fileUrl = "/uploads/" + fileName;

        MediaUploadStrategy selectedStrategy = mediaUploadStrategies.stream()
                .filter(strategy -> strategy.supports(file))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Only image or video files are allowed"));

        selectedStrategy.handleMedia(advertisement, fileUrl);

        return advertisementService.updateAdvertisement(id, advertisement);
    }
}
