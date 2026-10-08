package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsActiveTrue();

    List<Product> findByBrandIgnoreCase(String brand);

    Optional<Product> findByCodeIgnoreCase(String code);
}
