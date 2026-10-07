package com.example.franquicias.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.franquicias.dto.BranchResponse;
import com.example.franquicias.dto.CreateBranchRequest;
import com.example.franquicias.dto.UpdateBranchRequest;
import com.example.franquicias.entity.Branch;
import com.example.franquicias.entity.Franchise;
import com.example.franquicias.exception.ResourceNotFoundException;
import com.example.franquicias.mapper.BranchMapper;
import com.example.franquicias.repository.BranchRepository;
import com.example.franquicias.repository.FranchiseRepository;
import com.example.franquicias.service.impl.BranchServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchMapper branchMapper;

    @InjectMocks
    private BranchServiceImpl branchService;

    private Franchise franchise;
    private Branch branch;

    @BeforeEach
    void setUp() {
        franchise = Franchise.builder().id(1L).name("Franquicia Principal").build();
        branch = Branch.builder().id(10L).name("Medellin").franchise(franchise).build();
    }

    @Test
    void create_whenFranchiseExists_savesBranch() {
        CreateBranchRequest request = new CreateBranchRequest("Medellin");
        BranchResponse expected = new BranchResponse(10L, "Medellin", 1L);

        when(franchiseRepository.findById(1L)).thenReturn(Optional.of(franchise));
        when(branchRepository.save(any(Branch.class))).thenReturn(branch);
        when(branchMapper.toResponse(branch)).thenReturn(expected);

        BranchResponse result = branchService.create(1L, request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void create_whenFranchiseDoesNotExist_throwsNotFoundAndDoesNotSave() {
        CreateBranchRequest request = new CreateBranchRequest("Medellin");
        when(franchiseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> branchService.create(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(branchRepository, never()).save(any());
    }

    @Test
    void updateName_whenBranchExists_updatesAndReturns() {
        UpdateBranchRequest request = new UpdateBranchRequest("Nueva sucursal");
        BranchResponse expected = new BranchResponse(10L, "Nueva sucursal", 1L);

        when(branchRepository.findById(10L)).thenReturn(Optional.of(branch));
        when(branchRepository.save(branch)).thenReturn(branch);
        when(branchMapper.toResponse(branch)).thenReturn(expected);

        BranchResponse result = branchService.updateName(10L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(branch.getName()).isEqualTo("Nueva sucursal");
    }

    @Test
    void updateName_whenBranchDoesNotExist_throwsNotFound() {
        UpdateBranchRequest request = new UpdateBranchRequest("Nueva sucursal");
        when(branchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> branchService.updateName(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getEntitiesByFranchiseId_returnsBranchesFromRepository() {
        when(branchRepository.findByFranchiseId(1L)).thenReturn(List.of(branch));

        List<Branch> result = branchService.getEntitiesByFranchiseId(1L);

        assertThat(result).containsExactly(branch);
    }
}
