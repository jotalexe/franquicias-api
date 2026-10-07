package com.example.franquicias.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.franquicias.dto.CreateProductRequest;
import com.example.franquicias.dto.ProductResponse;
import com.example.franquicias.dto.UpdateProductRequest;
import com.example.franquicias.dto.UpdateStockRequest;
import com.example.franquicias.entity.Branch;
import com.example.franquicias.entity.Franchise;
import com.example.franquicias.entity.Product;
import com.example.franquicias.exception.ResourceNotFoundException;
import com.example.franquicias.mapper.ProductMapper;
import com.example.franquicias.repository.BranchRepository;
import com.example.franquicias.repository.ProductRepository;
import com.example.franquicias.service.impl.ProductServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Branch branch;
    private Product product;

    @BeforeEach
    void setUp() {
        Franchise franchise = Franchise.builder().id(1L).name("Franquicia Principal").build();
        branch = Branch.builder().id(10L).name("Medellin").franchise(franchise).build();
        product = Product.builder().id(100L).name("Producto A").stock(100).branch(branch).build();
    }

    @Test
    void create_whenBranchExists_savesProduct() {
        CreateProductRequest request = new CreateProductRequest("Producto A", 100);
        ProductResponse expected = new ProductResponse(100L, "Producto A", 100, 10L);

        when(branchRepository.findById(10L)).thenReturn(Optional.of(branch));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expected);

        ProductResponse result = productService.create(10L, request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void create_whenBranchDoesNotExist_throwsNotFoundAndDoesNotSave() {
        CreateProductRequest request = new CreateProductRequest("Producto A", 100);
        when(branchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_whenProductBelongsToBranch_deletesIt() {
        when(branchRepository.existsById(10L)).thenReturn(true);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        productService.delete(10L, 100L);

        verify(productRepository).delete(product);
    }

    @Test
    void delete_whenBranchDoesNotExist_throwsNotFound() {
        when(branchRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).delete(any());
    }

    @Test
    void delete_whenProductDoesNotExist_throwsNotFound() {
        when(branchRepository.existsById(10L)).thenReturn(true);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(10L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenProductBelongsToAnotherBranch_throwsNotFound() {
        when(branchRepository.existsById(20L)).thenReturn(true);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.delete(20L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).delete(any());
    }

    @Test
    void updateStock_whenProductExists_updatesAndReturns() {
        UpdateStockRequest request = new UpdateStockRequest(250);
        ProductResponse expected = new ProductResponse(100L, "Producto A", 250, 10L);

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expected);

        ProductResponse result = productService.updateStock(100L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(product.getStock()).isEqualTo(250);
    }

    @Test
    void updateStock_whenProductDoesNotExist_throwsNotFound() {
        UpdateStockRequest request = new UpdateStockRequest(250);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateStock(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateName_whenProductExists_updatesAndReturns() {
        UpdateProductRequest request = new UpdateProductRequest("Nuevo producto");
        ProductResponse expected = new ProductResponse(100L, "Nuevo producto", 100, 10L);

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expected);

        ProductResponse result = productService.updateName(100L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(product.getName()).isEqualTo("Nuevo producto");
    }

    @Test
    void findMaxStockProduct_whenBranchHasProducts_returnsTop() {
        ProductResponse expected = new ProductResponse(100L, "Producto A", 100, 10L);
        when(productRepository.findTopByBranchIdOrderByStockDescIdAsc(10L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(expected);

        Optional<ProductResponse> result = productService.findMaxStockProduct(10L);

        assertThat(result).contains(expected);
    }

    @Test
    void findMaxStockProduct_whenBranchHasNoProducts_returnsEmpty() {
        when(productRepository.findTopByBranchIdOrderByStockDescIdAsc(20L)).thenReturn(Optional.empty());

        Optional<ProductResponse> result = productService.findMaxStockProduct(20L);

        assertThat(result).isEmpty();
    }
}
