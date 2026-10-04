package com.eduframepackage.eduframe.quiz.controller;

import com.eduframepackage.eduframe.quiz.entity.Certificate;
import com.eduframepackage.eduframe.quiz.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping("/certificates/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        Certificate certificate = certificateService.getById(id);
        String filename = "certificate-" + certificate.getCertificateCode() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(certificate.getPdfContent());
    }
}
