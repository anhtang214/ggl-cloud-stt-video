package com.speechrecognition.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import com.speechrecognition.app.model.Transcription;
import com.speechrecognition.app.model.User;
import com.speechrecognition.app.repository.UserRepository;
import com.speechrecognition.app.service.TranscriptionService;
import java.util.List;

@RestController
@RequestMapping("/api/transcriptions")
public class TranscriptionController {
    
    @Autowired
    private TranscriptionService transcriptionService;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<List<Transcription>> getUserTranscriptions(
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        
        String email = oAuth2User.getAttribute("email");
        
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Transcription> transcriptions = 
            transcriptionService.getUserTranscriptions(user.getId());
        
        return ResponseEntity.ok(transcriptions);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<Transcription>> getCompletedTranscriptions(
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        
        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Transcription> transcriptions = 
            transcriptionService.getCompletedTranscriptions(user.getId());
        
        return ResponseEntity.ok(transcriptions);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getTranscriptionCount(
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        
        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        Long count = transcriptionService.getUserTranscriptionCount(user.getId());
        
        return ResponseEntity.ok(count);
    }
}