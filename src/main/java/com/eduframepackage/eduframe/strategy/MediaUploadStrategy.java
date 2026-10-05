package com.eduframepackage.eduframe.strategy;

import com.eduframepackage.eduframe.model.Advertisement;
import org.springframework.web.multipart.MultipartFile;

public interface MediaUploadStrategy {

    boolean supports(MultipartFile file);

    void handleMedia(Advertisement advertisement, String fileUrl);
}
