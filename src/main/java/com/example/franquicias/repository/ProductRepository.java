package com.example.franquicias.repository;

import com.example.franquicias.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByBranchId(Long branchId);

    boolean existsByIdAndBranchId(Long id, Long branchId);

    /**
     * Producto con mayor stock de una sucursal. Empate de stock se resuelve
     * de forma deterministica por el id mas bajo (orden de creacion).
     */
    Optional<Product> findTopByBranchIdOrderByStockDescIdAsc(Long branchId);
}
