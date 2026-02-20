package com.speechrecognition.app.config;

import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GcpConfig {

    @Value("${gcp.credentials.location}")
    private String credentialsPath;

    @Bean
    public ServiceAccountCredentials gcpCredentials() throws IOException {
        try (FileInputStream stream = new FileInputStream(credentialsPath)) {
            return (ServiceAccountCredentials) ServiceAccountCredentials
                    .fromStream(stream)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");
        }
    }
}
