package com.example.franquicias.service;

import com.example.franquicias.dto.CreateProductRequest;
import com.example.franquicias.dto.ProductResponse;
import com.example.franquicias.dto.UpdateProductRequest;
import com.example.franquicias.dto.UpdateStockRequest;
import java.util.Optional;

public interface ProductService {

    ProductResponse create(Long branchId, CreateProductRequest request);

    void delete(Long branchId, Long productId);

    ProductResponse updateStock(Long productId, UpdateStockRequest request);

    ProductResponse updateName(Long productId, UpdateProductRequest request);

    /**
     * Producto con mayor stock de una sucursal. Vacio si la sucursal no
     * tiene productos. Empate de stock: id mas bajo.
     */
    Optional<ProductResponse> findMaxStockProduct(Long branchId);
}
