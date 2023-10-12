package com.example.letsplay.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.letsplay.domain.Product;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByUserId(String userId);
}

