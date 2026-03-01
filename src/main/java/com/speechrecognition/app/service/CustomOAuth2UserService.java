package com.speechrecognition.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.speechrecognition.app.model.AuthProvider;
import com.speechrecognition.app.model.User;
import com.speechrecognition.app.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		
		String email = oAuth2User.getAttribute("email");
		String name = oAuth2User.getAttribute("name");
		String googleId = oAuth2User.getAttribute("sub");
		String picture = oAuth2User.getAttribute("picture");
		
		processOAuthUser(email, name, googleId, picture);
		
		return oAuth2User;
	}

	public User getByOAuth2User(OAuth2User oAuth2User) {
		String email = oAuth2User.getAttribute("email");
        return userRepository.findByEmail(email);
	}
	
	/**
	 * Creates or updates a user profile based on OAuth2 authentication data.
	 *
	 * If the user does not already exist in the system, a new user profile
	 * is created using the provided information.
	 *
	 * If the user already exists and this method is invoked, it is treated
	 * as an update operation, indicating that the user intends to update their
	 * profile information such as name, profile picture, or both.
	 */
	private void processOAuthUser(String email, String name, String googleId, String picture) {
		User existingUser = userRepository.findByEmail(email);
		
		if (existingUser == null) {
			User newUser = new User();
			newUser.setEmail(email);
			newUser.setFullName(name);
            newUser.setGoogleId(googleId);
            newUser.setProfilePictureUrl(picture);
            newUser.setAuthProvider(AuthProvider.GOOGLE);
            
            userRepository.save(newUser);
		}
	}
}
