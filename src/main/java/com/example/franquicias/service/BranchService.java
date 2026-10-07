package com.example.franquicias.service;

import com.example.franquicias.dto.BranchResponse;
import com.example.franquicias.dto.CreateBranchRequest;
import com.example.franquicias.dto.UpdateBranchRequest;
import com.example.franquicias.entity.Branch;
import java.util.List;

public interface BranchService {

    BranchResponse create(Long franchiseId, CreateBranchRequest request);

    BranchResponse updateName(Long branchId, UpdateBranchRequest request);

    /**
     * Devuelve la entidad o lanza ResourceNotFoundException. Se expone a
     * otros services (p. ej. ProductService) para validar existencia sin
     * duplicar la consulta.
     */
    Branch getEntityById(Long branchId);

    List<Branch> getEntitiesByFranchiseId(Long franchiseId);
}
