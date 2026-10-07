package com.example.franquicias.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representacion de un producto")
public record ProductResponse(
        Long id,
        String name,
        Integer stock,
        Long branchId
) {
}
