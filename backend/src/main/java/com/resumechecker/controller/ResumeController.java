package com.resumechecker.controller;

import com.resumechecker.model.ResumeRequest;
import com.resumechecker.model.ResumeResponse;
import com.resumechecker.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/analyze")
    public ResponseEntity<ResumeResponse> analyzeResume(@RequestBody ResumeRequest request) {
        if (request.getResumeText() == null || request.getResumeText().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ResumeResponse response = resumeService.analyzeResume(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Resume Checker API is running");
    }
}
