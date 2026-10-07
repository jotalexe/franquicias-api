package com.example.franquicias.controller;

import com.example.franquicias.dto.CreateFranchiseRequest;
import com.example.franquicias.dto.ErrorResponse;
import com.example.franquicias.dto.FranchiseResponse;
import com.example.franquicias.dto.MaxStockProductResponse;
import com.example.franquicias.dto.UpdateFranchiseRequest;
import com.example.franquicias.service.FranchiseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/franchises")
@RequiredArgsConstructor
@Tag(name = "Franchises", description = "Gestion de franquicias")
public class FranchiseController {

    private final FranchiseService franchiseService;

    @PostMapping
    @Operation(summary = "Crear franquicia")
    @ApiResponse(responseCode = "201", description = "Franquicia creada",
            content = @Content(schema = @Schema(implementation = FranchiseResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<FranchiseResponse> create(@Valid @RequestBody CreateFranchiseRequest request) {
        FranchiseResponse response = franchiseService.create(request);
        return ResponseEntity.created(URI.create("/api/franchises/" + response.id())).body(response);
    }

    @PatchMapping("/{franchiseId}")
    @Operation(summary = "Actualizar nombre de franquicia")
    @ApiResponse(responseCode = "200", description = "Franquicia actualizada",
            content = @Content(schema = @Schema(implementation = FranchiseResponse.class)))
    @ApiResponse(responseCode = "404", description = "Franquicia no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<FranchiseResponse> updateName(
            @PathVariable Long franchiseId, @Valid @RequestBody UpdateFranchiseRequest request) {
        return ResponseEntity.ok(franchiseService.updateName(franchiseId, request));
    }

    @GetMapping("/{franchiseId}/products/max-stock")
    @Operation(summary = "Producto con mayor stock por cada sucursal de la franquicia",
            description = "Las sucursales sin productos se excluyen del resultado. "
                    + "En caso de empate de stock se elige el producto con menor id.")
    @ApiResponse(responseCode = "200", description = "Listado de productos con mayor stock por sucursal",
            content = @Content(schema = @Schema(implementation = MaxStockProductResponse.class)))
    @ApiResponse(responseCode = "404", description = "Franquicia no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<MaxStockProductResponse>> getMaxStockProducts(@PathVariable Long franchiseId) {
        return ResponseEntity.ok(franchiseService.getMaxStockProductsByBranch(franchiseId));
    }
}
