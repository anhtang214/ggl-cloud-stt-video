package com.speechrecognition.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.speechrecognition.app.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA will automatically implement these methods
    User findByEmail(String email);
    boolean existsByEmail(String email);
}
