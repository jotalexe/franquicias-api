package com.example.franquicias.controller;

import com.example.franquicias.dto.CreateProductRequest;
import com.example.franquicias.dto.ErrorResponse;
import com.example.franquicias.dto.ProductResponse;
import com.example.franquicias.dto.UpdateProductRequest;
import com.example.franquicias.dto.UpdateStockRequest;
import com.example.franquicias.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Gestion de productos")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/branches/{branchId}/products")
    @Operation(summary = "Crear producto en una sucursal")
    @ApiResponse(responseCode = "201", description = "Producto creado",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos invalidos (nombre vacio o stock negativo)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Sucursal no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ProductResponse> create(
            @PathVariable Long branchId, @Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.create(branchId, request);
        return ResponseEntity.created(URI.create("/api/products/" + response.id())).body(response);
    }

    @DeleteMapping("/branches/{branchId}/products/{productId}")
    @Operation(summary = "Eliminar producto de una sucursal",
            description = "Valida que la sucursal exista, que el producto exista y que pertenezca a esa sucursal.")
    @ApiResponse(responseCode = "204", description = "Producto eliminado")
    @ApiResponse(responseCode = "404", description = "Sucursal o producto no encontrado, o el producto no pertenece a la sucursal",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable Long branchId, @PathVariable Long productId) {
        productService.delete(branchId, productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/products/{productId}/stock")
    @Operation(summary = "Actualizar stock de un producto")
    @ApiResponse(responseCode = "200", description = "Stock actualizado",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Stock negativo",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long productId, @Valid @RequestBody UpdateStockRequest request) {
        return ResponseEntity.ok(productService.updateStock(productId, request));
    }

    @PatchMapping("/products/{productId}")
    @Operation(summary = "Actualizar nombre de un producto")
    @ApiResponse(responseCode = "200", description = "Producto actualizado",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ProductResponse> updateName(
            @PathVariable Long productId, @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateName(productId, request));
    }
}
