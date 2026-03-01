package com.speechrecognition.app.controller;

import com.speechrecognition.app.service.VideoProcessingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UploadController {
    @Autowired
    private VideoProcessingService videoProcessingService;

    @GetMapping("/upload")
    public String uploadForm() {
        return "video-upload";
    }
}