package com.example.franquicias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateBranchRequest(

        @Schema(description = "Nuevo nombre de la sucursal", example = "Nueva sucursal")
        @NotBlank(message = "El nombre de la sucursal es obligatorio")
        String name
) {
}
