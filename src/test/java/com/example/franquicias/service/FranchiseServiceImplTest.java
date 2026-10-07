package com.example.franquicias.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.franquicias.dto.CreateFranchiseRequest;
import com.example.franquicias.dto.FranchiseResponse;
import com.example.franquicias.dto.MaxStockProductResponse;
import com.example.franquicias.dto.ProductResponse;
import com.example.franquicias.dto.UpdateFranchiseRequest;
import com.example.franquicias.entity.Branch;
import com.example.franquicias.entity.Franchise;
import com.example.franquicias.exception.ResourceNotFoundException;
import com.example.franquicias.mapper.FranchiseMapper;
import com.example.franquicias.repository.FranchiseRepository;
import com.example.franquicias.service.impl.FranchiseServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceImplTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private FranchiseMapper franchiseMapper;

    @Mock
    private BranchService branchService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private FranchiseServiceImpl franchiseService;

    private Franchise franchise;

    @BeforeEach
    void setUp() {
        franchise = Franchise.builder().id(1L).name("Franquicia Principal").build();
    }

    @Test
    void create_savesAndReturnsFranchise() {
        CreateFranchiseRequest request = new CreateFranchiseRequest("Franquicia Principal");
        Franchise saved = Franchise.builder().id(1L).name("Franquicia Principal").build();
        FranchiseResponse expected = new FranchiseResponse(1L, "Franquicia Principal");

        when(franchiseRepository.save(any(Franchise.class))).thenReturn(saved);
        when(franchiseMapper.toResponse(saved)).thenReturn(expected);

        FranchiseResponse result = franchiseService.create(request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void updateName_whenFranchiseExists_updatesAndReturns() {
        UpdateFranchiseRequest request = new UpdateFranchiseRequest("Nuevo nombre");
        FranchiseResponse expected = new FranchiseResponse(1L, "Nuevo nombre");

        when(franchiseRepository.findById(1L)).thenReturn(Optional.of(franchise));
        when(franchiseRepository.save(franchise)).thenReturn(franchise);
        when(franchiseMapper.toResponse(franchise)).thenReturn(expected);

        FranchiseResponse result = franchiseService.updateName(1L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(franchise.getName()).isEqualTo("Nuevo nombre");
    }

    @Test
    void updateName_whenFranchiseDoesNotExist_throwsNotFound() {
        UpdateFranchiseRequest request = new UpdateFranchiseRequest("Nuevo nombre");
        when(franchiseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> franchiseService.updateName(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMaxStockProductsByBranch_whenFranchiseDoesNotExist_throwsNotFound() {
        when(franchiseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> franchiseService.getMaxStockProductsByBranch(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(branchService, productService);
    }

    @Test
    void getMaxStockProductsByBranch_whenFranchiseHasNoBranches_returnsEmptyList() {
        when(franchiseRepository.findById(1L)).thenReturn(Optional.of(franchise));
        when(branchService.getEntitiesByFranchiseId(1L)).thenReturn(List.of());

        List<MaxStockProductResponse> result = franchiseService.getMaxStockProductsByBranch(1L);

        assertThat(result).isEmpty();
        verifyNoInteractions(productService);
    }

    @Test
    void getMaxStockProductsByBranch_skipsBranchesWithoutProducts() {
        Branch withProducts = Branch.builder().id(10L).name("Medellin").franchise(franchise).build();
        Branch withoutProducts = Branch.builder().id(20L).name("Bogota").franchise(franchise).build();
        ProductResponse topProduct = new ProductResponse(100L, "Producto B", 250, 10L);

        when(franchiseRepository.findById(1L)).thenReturn(Optional.of(franchise));
        when(branchService.getEntitiesByFranchiseId(1L)).thenReturn(List.of(withProducts, withoutProducts));
        when(productService.findMaxStockProduct(10L)).thenReturn(Optional.of(topProduct));
        when(productService.findMaxStockProduct(20L)).thenReturn(Optional.empty());

        List<MaxStockProductResponse> result = franchiseService.getMaxStockProductsByBranch(1L);

        assertThat(result).hasSize(1);
        MaxStockProductResponse response = result.get(0);
        assertThat(response.branchId()).isEqualTo(10L);
        assertThat(response.branchName()).isEqualTo("Medellin");
        assertThat(response.productId()).isEqualTo(100L);
        assertThat(response.productName()).isEqualTo("Producto B");
        assertThat(response.stock()).isEqualTo(250);
        verify(productService).findMaxStockProduct(20L);
    }
}
