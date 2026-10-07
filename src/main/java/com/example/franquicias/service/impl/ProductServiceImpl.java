package com.example.franquicias.service.impl;

import com.example.franquicias.dto.CreateProductRequest;
import com.example.franquicias.dto.ProductResponse;
import com.example.franquicias.dto.UpdateProductRequest;
import com.example.franquicias.dto.UpdateStockRequest;
import com.example.franquicias.entity.Branch;
import com.example.franquicias.entity.Product;
import com.example.franquicias.exception.ResourceNotFoundException;
import com.example.franquicias.mapper.ProductMapper;
import com.example.franquicias.repository.BranchRepository;
import com.example.franquicias.repository.ProductRepository;
import com.example.franquicias.service.ProductService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Depende directamente de BranchRepository (no de BranchService) por la
 * misma razon documentada en BranchServiceImpl: evitar un ciclo de beans
 * con FranchiseService, que orquesta tanto BranchService como
 * ProductService para el calculo de max-stock.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse create(Long branchId, CreateProductRequest request) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> ResourceNotFoundException.branch(branchId));

        Product product = Product.builder()
                .name(request.name())
                .stock(request.stock())
                .branch(branch)
                .build();

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public void delete(Long branchId, Long productId) {
        if (!branchRepository.existsById(branchId)) {
            throw ResourceNotFoundException.branch(branchId);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));

        if (!product.getBranch().getId().equals(branchId)) {
            throw ResourceNotFoundException.productInBranch(productId, branchId);
        }

        productRepository.delete(product);
    }

    @Override
    public ProductResponse updateStock(Long productId, UpdateStockRequest request) {
        Product product = findEntityById(productId);
        product.setStock(request.stock());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateName(Long productId, UpdateProductRequest request) {
        Product product = findEntityById(productId);
        product.setName(request.name());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponse> findMaxStockProduct(Long branchId) {
        return productRepository.findTopByBranchIdOrderByStockDescIdAsc(branchId)
                .map(productMapper::toResponse);
    }

    private Product findEntityById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));
    }
}
