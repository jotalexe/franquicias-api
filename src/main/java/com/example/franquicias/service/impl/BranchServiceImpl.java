package com.example.franquicias.service.impl;

import com.example.franquicias.dto.BranchResponse;
import com.example.franquicias.dto.CreateBranchRequest;
import com.example.franquicias.dto.UpdateBranchRequest;
import com.example.franquicias.entity.Branch;
import com.example.franquicias.entity.Franchise;
import com.example.franquicias.exception.ResourceNotFoundException;
import com.example.franquicias.mapper.BranchMapper;
import com.example.franquicias.repository.BranchRepository;
import com.example.franquicias.repository.FranchiseRepository;
import com.example.franquicias.service.BranchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Depende directamente de FranchiseRepository (no de FranchiseService) para
 * validar la existencia de la franquicia padre. Evita una dependencia
 * circular entre services (FranchiseService orquesta BranchService para el
 * calculo de max-stock, asi que BranchService no puede depender de vuelta
 * de FranchiseService).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final FranchiseRepository franchiseRepository;
    private final BranchMapper branchMapper;

    @Override
    public BranchResponse create(Long franchiseId, CreateBranchRequest request) {
        Franchise franchise = franchiseRepository.findById(franchiseId)
                .orElseThrow(() -> ResourceNotFoundException.franchise(franchiseId));

        Branch branch = Branch.builder()
                .name(request.name())
                .franchise(franchise)
                .build();

        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    public BranchResponse updateName(Long branchId, UpdateBranchRequest request) {
        Branch branch = getEntityById(branchId);
        branch.setName(request.name());
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional(readOnly = true)
    public Branch getEntityById(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> ResourceNotFoundException.branch(branchId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Branch> getEntitiesByFranchiseId(Long franchiseId) {
        return branchRepository.findByFranchiseId(franchiseId);
    }
}
