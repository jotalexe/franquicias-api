package com.example.franquicias.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Producto con mayor stock de una sucursal")
public record MaxStockProductResponse(
        Long branchId,
        String branchName,
        Long productId,
        String productName,
        Integer stock
) {
}
