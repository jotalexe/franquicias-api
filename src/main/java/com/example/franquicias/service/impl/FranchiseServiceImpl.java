package com.example.franquicias.service.impl;

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
import com.example.franquicias.service.BranchService;
import com.example.franquicias.service.FranchiseService;
import com.example.franquicias.service.ProductService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FranchiseServiceImpl implements FranchiseService {

    private final FranchiseRepository franchiseRepository;
    private final FranchiseMapper franchiseMapper;
    private final BranchService branchService;
    private final ProductService productService;

    @Override
    public FranchiseResponse create(CreateFranchiseRequest request) {
        Franchise franchise = Franchise.builder()
                .name(request.name())
                .build();

        return franchiseMapper.toResponse(franchiseRepository.save(franchise));
    }

    @Override
    public FranchiseResponse updateName(Long franchiseId, UpdateFranchiseRequest request) {
        Franchise franchise = getEntityById(franchiseId);
        franchise.setName(request.name());
        return franchiseMapper.toResponse(franchiseRepository.save(franchise));
    }

    @Override
    @Transactional(readOnly = true)
    public Franchise getEntityById(Long franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .orElseThrow(() -> ResourceNotFoundException.franchise(franchiseId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaxStockProductResponse> getMaxStockProductsByBranch(Long franchiseId) {
        getEntityById(franchiseId);

        return branchService.getEntitiesByFranchiseId(franchiseId).stream()
                .map(this::toMaxStockResponse)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<MaxStockProductResponse> toMaxStockResponse(Branch branch) {
        return productService.findMaxStockProduct(branch.getId())
                .map(product -> new MaxStockProductResponse(
                        branch.getId(), branch.getName(), product.id(), product.name(), product.stock()));
    }
}
