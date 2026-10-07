package com.example.franquicias.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representacion de una sucursal")
public record BranchResponse(
        Long id,
        String name,
        Long franchiseId
) {
}
