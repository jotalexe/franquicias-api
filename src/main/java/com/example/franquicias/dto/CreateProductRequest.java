package com.example.franquicias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(

        @Schema(description = "Nombre del producto", example = "Producto A")
        @NotBlank(message = "El nombre del producto es obligatorio")
        String name,

        @Schema(description = "Cantidad de stock inicial", example = "100")
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock
) {
}
