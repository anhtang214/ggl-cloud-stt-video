package com.speechrecognition.app.controller;

import com.speechrecognition.app.service.VideoProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class UploadController {
    @Autowired
    private VideoProcessingService videoProcessingService;
}