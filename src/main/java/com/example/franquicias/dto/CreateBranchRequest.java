package com.example.franquicias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateBranchRequest(

        @Schema(description = "Nombre de la sucursal", example = "Sucursal Medellin")
        @NotBlank(message = "El nombre de la sucursal es obligatorio")
        String name
) {
}
