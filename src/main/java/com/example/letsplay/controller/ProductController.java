package com.example.letsplay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.example.letsplay.domain.Product;
import com.example.letsplay.domain.ProductResponse;
import com.example.letsplay.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public List<ProductResponse> getAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable String id) {
        ProductResponse productResponse = productService.getProductById(id);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/byUserId/{userId}")
    public List<ProductResponse> getByUserId(@PathVariable String userId) {
        return productService.getProductsByUserId(userId);
    }

    @PostMapping("/product")
    public Product create(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid Product product) {
        return productService.createProduct(userDetails, product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody @Valid Product updatedProduct, Principal principal) {
        try {
            Product product = productService.updateProduct(id, updatedProduct, principal.getName());
            return ResponseEntity.ok("Product updated: " + product);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id, Principal principal) {
        try {
            productService.deleteProduct(id, principal.getName());
            return ResponseEntity.ok("Product deleted");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
    }
}
