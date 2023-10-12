package com.example.letsplay.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.letsplay.domain.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByName(String name);
    Optional<User> findUserByEmail(String email);

    @Query("{'productId.userId': ?0}")
    Optional<User> findByProductId(String userId);
    boolean existsByEmail(String adminEmail);
}
