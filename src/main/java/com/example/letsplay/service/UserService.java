package com.example.letsplay.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.letsplay.domain.Product;
import com.example.letsplay.domain.User;
import com.example.letsplay.domain.UserResponse;
import com.example.letsplay.enums.Role;
import com.example.letsplay.exception.ResourceNotFoundException;
import com.example.letsplay.repository.ProductRepository;
import com.example.letsplay.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Service
public class UserService {

    @Value("${admin.default.name}")
    private String adminName;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = users.stream()
                                               .map(this::mapToUserResponse)
                                               .collect(Collectors.toList());
        return userResponses;
    }

    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                                  .orElseThrow(() -> new ResourceNotFoundException("User not found for this id :: " + id));
        return mapToUserResponse(user);
    }

    public UserResponse getUserByProductId(String productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        String userId = product.getUserId();
        User user = userRepository.findById(userId).orElseThrow();
        return mapToUserResponse(user);
    }

    public UserResponse getUserByName(String name) {
        User user = userRepository.findByName(name).orElseThrow();
        return mapToUserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow();
        return mapToUserResponse(user);
    }

    public User updateUser(String id, User updatedUser, String authenticatedUserName) {
        User originalUser = userRepository.findById(id)
                                        .orElseThrow(() -> new ResourceNotFoundException("User not found for this id :: " + id));

        User authenticatedUser = userRepository.findByName(authenticatedUserName)
                                            .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found in database."));

        if (!authenticatedUser.getRole().equals(Role.ROLE_ADMIN) && !originalUser.getRole().equals(updatedUser.getRole())) {
            throw new AccessDeniedException("Only admins can change user roles");
        }

        // Now it's safe to set the ID and save the updated user
        updatedUser.setId(id);
        return userRepository.save(updatedUser);
    }


    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    private UserResponse mapToUserResponse(User user) {
        User authenticatedUser = getAuthenticatedUser();

        UserResponse.UserResponseBuilder responseBuilder = UserResponse.builder()
                                                                    .name(user.getName())
                                                                    .email(user.getEmail())
                                                                    .id("hidden");

        if (authenticatedUser != null) {
            boolean isAdmin = authenticatedUser.getRole().equals(Role.ROLE_ADMIN);
            boolean isSelf = user.getId().equals(authenticatedUser.getId());

            if (isAdmin || isSelf) {
                responseBuilder.id(user.getId());
            }
        }

        return responseBuilder.build();
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return null;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByName(userDetails.getUsername())
                            .orElse(null);
    }


    @PostConstruct
    public void initDefaultAdmin() {
        // Check if the default admin exists in the DB
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .name(adminName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }
}
