package com.speechrecognition.app.controller;

import com.speechrecognition.app.model.User;
import com.speechrecognition.app.service.CustomOAuth2UserService;
import com.speechrecognition.app.service.UploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @Autowired
    private CustomOAuth2UserService userService;

    @GetMapping("/upload")
    public String uploadForm() {
        return "video-upload";
    }

    @PostMapping("/upload")
    public String handleUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("language") String language,
            @RequestParam(value = "language2", required = false) String language2,
            @AuthenticationPrincipal OAuth2User oAuth2User) throws Exception {

        User user = userService.getByOAuth2User(oAuth2User);
        uploadService.fullUploadPipeline(user, file, title, language, language2);

        return "redirect:/dashboard";
    }
}