package com.example.franquicias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateFranchiseRequest(

        @Schema(description = "Nombre de la franquicia", example = "Franquicia Principal")
        @NotBlank(message = "El nombre de la franquicia es obligatorio")
        String name
) {
}
