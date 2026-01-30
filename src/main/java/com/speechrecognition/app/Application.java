package com.speechrecognition.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import com.speechrecognition.app.model.User;
import com.speechrecognition.app.repository.UserRepository;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    // This bean will run after the application starts
    @Bean
    public CommandLineRunner testDatabaseConnection(UserRepository userRepository) {
        return args -> {
            System.out.println("\n========================================");
            System.out.println("Testing Database Connection...");
            System.out.println("========================================\n");
            
            // Test 1: Count existing users
            long userCount = userRepository.count();
            System.out.println("✓ Current number of users in database: " + userCount);
            
            // Test 2: Create and save a test user
            User testUser = new User();
            testUser.setEmail("test@example.com");
            testUser.setPassword("testPassword123");
            
            try {
                User savedUser = userRepository.save(testUser);
                System.out.println("✓ Successfully created test user with ID: " + savedUser.getId());
                System.out.println("✓ User email: " + savedUser.getEmail());
                System.out.println("✓ Created at: " + savedUser.getCreatedAt());
            } catch (Exception e) {
                System.err.println("✗ Error creating test user: " + e.getMessage());
            }
            
            // Test 3: Retrieve all users
            System.out.println("\n--- All Users in Database ---");
            userRepository.findAll().forEach(user -> {
                System.out.println("ID: " + user.getId() + 
                                 ", Email: " + user.getEmail() + 
                                 ", Created: " + user.getCreatedAt());
            });
            
            System.out.println("\n========================================");
            System.out.println("Database Connection Test Complete!");
            System.out.println("========================================\n");
        };
    }
}