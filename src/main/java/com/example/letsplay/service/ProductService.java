package com.example.letsplay.service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.letsplay.domain.Product;
import com.example.letsplay.domain.ProductResponse;
import com.example.letsplay.domain.User;
import com.example.letsplay.enums.Role;
import com.example.letsplay.exception.ResourceNotFoundException;
import com.example.letsplay.repository.ProductRepository;
import com.example.letsplay.repository.UserRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductResponse> productResponses = products.stream()
                                                        .map(this::mapToProductResponse)
                                                        .collect(Collectors.toList());
        return productResponses;
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found for this id :: " + id));
        return mapToProductResponse(product);
    }

    public List<ProductResponse> getProductsByUserId(String userId) {
        List<Product> products =  productRepository.findByUserId(userId);
        List<ProductResponse> productResponses = products.stream()
                                                        .map(this::mapToProductResponse)
                                                        .collect(Collectors.toList());
        return productResponses;
    }

    public Product createProduct(UserDetails userDetails, Product product) {
        if (userDetails == null) {
            throw new UsernameNotFoundException("You must be authenticated to create a product.");
        }

        User user = userRepository.findByName(userDetails.getUsername())
                                  .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found in database."));
        product.setUserId(user.getId());

        return productRepository.save(product);
    }

    public Product updateProduct(String id, Product updatedProduct, String authenticatedUserName) throws AccessDeniedException {
        Product product = productRepository.findById(id)
                                           .orElseThrow(() -> new ResourceNotFoundException("Product not found for this id :: " + id));
        User authenticatedUser = userRepository.findByName(authenticatedUserName)
                                              .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean isAdmin = authenticatedUser.getRole().equals(Role.ROLE_ADMIN);
        boolean isOwner = productRepository.findById(id).get().getUserId().equals(authenticatedUser.getId());

        if (isAdmin || isOwner) {
            updatedProduct.setId(id);
            updatedProduct.setUserId(product.getUserId());
            if (updatedProduct.getName() == null) {
                updatedProduct.setName(product.getName());
            }
            if (updatedProduct.getDescription() == null) {
                updatedProduct.setDescription(product.getDescription());
            }
            return productRepository.save(updatedProduct);
        } else {
            throw new AccessDeniedException("Access denied");
        }
    }

    public void deleteProduct(String id, String authenticatedUserName) throws AccessDeniedException {
        User authenticatedUser = userRepository.findByName(authenticatedUserName)
                                              .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean isAdmin = authenticatedUser.getRole().equals(Role.ROLE_ADMIN);
        String productId = productRepository.findById(id).orElse(new Product()).getUserId();
        String authUserId = authenticatedUser.getId();
        boolean isOwner = false;
        if (productId != null && authUserId != null) {
            isOwner = productId.equals(authUserId);
        }

        if (isAdmin || isOwner) {
            productRepository.deleteById(id);
        } else {
            throw new AccessDeniedException("Access denied");
        }
    }

    private ProductResponse mapToProductResponse(Product product) {
        User authenticatedUser = getAuthenticatedUser();

        ProductResponse.ProductResponseBuilder responseBuilder = ProductResponse.builder()
                                                                                 .id("hidden")
                                                                                 .name(product.getName())
                                                                                 .description(product.getDescription())
                                                                                 .price(product.getPrice())
                                                                                 .userId("hidden");

        if (authenticatedUser != null) {
            boolean isAdmin = authenticatedUser.getRole().equals(Role.ROLE_ADMIN);
            boolean isOwner = authenticatedUser.getId().equals(product.getUserId());

            if (isAdmin || isOwner) {
                responseBuilder.id(product.getId())
                               .userId(product.getUserId());
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

}
