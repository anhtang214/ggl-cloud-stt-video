package com.speechrecognition.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import com.speechrecognition.app.dto.ProjectDTO;
import com.speechrecognition.app.dto.ProjectDetailDTO;
import com.speechrecognition.app.dto.TranscriptionSegmentDTO;
import com.speechrecognition.app.model.Project;
import com.speechrecognition.app.model.User;
import com.speechrecognition.app.service.CloudStorageService;
import com.speechrecognition.app.service.CustomOAuth2UserService;
import com.speechrecognition.app.service.ProjectService;
import com.speechrecognition.app.service.TranscriptionSegmentService;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private CustomOAuth2UserService userService;

    @Autowired
    private TranscriptionSegmentService segmentService;

    @Autowired
    private CloudStorageService cloudStorageService;
    
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getUserProjects(
            @AuthenticationPrincipal OAuth2User oAuth2User) {

        User user = userService.getByOAuth2User(oAuth2User);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<ProjectDTO> projects = projectService.getUserProjects(user.getId())
            .stream()
            .map(ProjectDTO::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailDTO> getProjectDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User oAuth2User) {

        // 1. Get the logged-in user
        User user = userService.getByOAuth2User(oAuth2User);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. Find the project
         Project project = projectService.getProjectById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. Verify the project belongs to this user
        if (!project.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        // 4. Get transcript segments (ordered by segmentIndex)
        List<TranscriptionSegmentDTO> segments = segmentService
            .getSegmentsByProject(id)
            .stream()
            .map(TranscriptionSegmentDTO::fromEntity)
            .collect(Collectors.toList());

        // 5. Get signed video URL (1-hour expiry)
        String videoUrl = null;
        if (project.getGcsUri() != null) {
            try {
                videoUrl = cloudStorageService.getSignedUrl(project.getGcsUri());
            } catch (Exception e) {
                System.out.println("Could not generate signed URL: " + e.getMessage());
            }
        }

        // 6. Bundle everything into one DTO
        ProjectDetailDTO detail = ProjectDetailDTO.fromEntity(project, videoUrl, segments);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<ProjectDTO>> getCompletedProjects(
            @AuthenticationPrincipal OAuth2User oAuth2User) {

        User user = userService.getByOAuth2User(oAuth2User);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<ProjectDTO> projects = projectService.getCompletedProjects(user.getId())
            .stream()
            .map(ProjectDTO::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getProjectCount(
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        
        User user = userService.getByOAuth2User(oAuth2User);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        Long count = projectService.getUserProjectCount(user.getId());
        
        return ResponseEntity.ok(count);
    }
}