package com.speechrecognition.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.speechrecognition.app.dto.UserDTO;
import com.speechrecognition.app.model.User;
import com.speechrecognition.app.service.CustomOAuth2UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private CustomOAuth2UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            @AuthenticationPrincipal OAuth2User oAuth2User) {

        User user = userService.getByOAuth2User(oAuth2User);

        if (user == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }
}