package com.speechrecognition.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.speechrecognition.app.dto.UserDTO;
import com.speechrecognition.app.model.User;
import com.speechrecognition.app.service.CustomOAuth2UserService;

@ControllerAdvice
public class GlobalModelAttributes {
    @Autowired
    private CustomOAuth2UserService userService;

    @ModelAttribute("currentUser")
    public UserDTO currentUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) return UserDTO.fromEntity(new User());

        User user = userService.getByOAuth2User(oAuth2User);

        if (user == null) return UserDTO.fromEntity(new User());

        return UserDTO.fromEntity(user);
    }
}