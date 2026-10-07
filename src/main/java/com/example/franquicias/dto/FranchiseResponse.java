package com.example.franquicias.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representacion de una franquicia")
public record FranchiseResponse(
        Long id,
        String name
) {
}
