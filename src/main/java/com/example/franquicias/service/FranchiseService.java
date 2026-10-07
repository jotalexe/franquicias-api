package com.example.franquicias.service;

import com.example.franquicias.dto.CreateFranchiseRequest;
import com.example.franquicias.dto.FranchiseResponse;
import com.example.franquicias.dto.MaxStockProductResponse;
import com.example.franquicias.dto.UpdateFranchiseRequest;
import com.example.franquicias.entity.Franchise;
import java.util.List;

public interface FranchiseService {

    FranchiseResponse create(CreateFranchiseRequest request);

    FranchiseResponse updateName(Long franchiseId, UpdateFranchiseRequest request);

    /**
     * Devuelve la entidad o lanza ResourceNotFoundException. Se expone a
     * otros services (p. ej. BranchService) para validar existencia sin
     * duplicar la consulta.
     */
    Franchise getEntityById(Long franchiseId);

    /**
     * Producto con mayor stock de cada sucursal de la franquicia. Las
     * sucursales sin productos se excluyen del resultado (ver README).
     */
    List<MaxStockProductResponse> getMaxStockProductsByBranch(Long franchiseId);
}
