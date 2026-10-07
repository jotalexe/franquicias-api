package com.example.franquicias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateFranchiseRequest(

        @Schema(description = "Nuevo nombre de la franquicia", example = "Nuevo nombre")
        @NotBlank(message = "El nombre de la franquicia es obligatorio")
        String name
) {
}
