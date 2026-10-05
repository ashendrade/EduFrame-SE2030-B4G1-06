package com.eduframepackage.eduframe.strategy;

import com.eduframepackage.eduframe.model.Advertisement;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class VideoUploadStrategy implements MediaUploadStrategy {

    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("video/");
    }

    @Override
    public void handleMedia(Advertisement advertisement, String fileUrl) {
        advertisement.setVideoUrl(fileUrl);
        advertisement.setImageUrl(null);
        advertisement.setMediaType("VIDEO");
    }
}
