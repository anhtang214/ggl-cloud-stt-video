package com.speechrecognition.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import com.speechrecognition.app.model.Project;
import com.speechrecognition.app.model.User;
import com.speechrecognition.app.repository.UserRepository;
import com.speechrecognition.app.service.ProjectService;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<List<Project>> getUserTranscriptions(
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        
        String email = oAuth2User.getAttribute("email");
        
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Project> transcriptions = 
            projectService.getUserProjects(user.getId());
        
        return ResponseEntity.ok(transcriptions);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<Project>> getCompletedProjects(
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        
        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Project> projects = 
            projectService.getCompletedProjects(user.getId());
        
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getProjectCount(
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        
        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        Long count = projectService.getUserProjectCount(user.getId());
        
        return ResponseEntity.ok(count);
    }
}