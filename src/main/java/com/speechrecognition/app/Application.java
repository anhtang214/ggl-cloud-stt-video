package com.speechrecognition.app;

import com.speechrecognition.app.config.StorageProperties;
import com.speechrecognition.app.service.storage.StorageService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import com.speechrecognition.app.model.User;
import com.speechrecognition.app.repository.UserRepository;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}