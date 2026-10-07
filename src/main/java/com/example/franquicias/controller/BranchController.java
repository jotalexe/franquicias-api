package com.example.franquicias.controller;

import com.example.franquicias.dto.BranchResponse;
import com.example.franquicias.dto.CreateBranchRequest;
import com.example.franquicias.dto.ErrorResponse;
import com.example.franquicias.dto.UpdateBranchRequest;
import com.example.franquicias.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Gestion de sucursales")
public class BranchController {

    private final BranchService branchService;

    @PostMapping("/franchises/{franchiseId}/branches")
    @Operation(summary = "Crear sucursal para una franquicia")
    @ApiResponse(responseCode = "201", description = "Sucursal creada",
            content = @Content(schema = @Schema(implementation = BranchResponse.class)))
    @ApiResponse(responseCode = "404", description = "Franquicia no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BranchResponse> create(
            @PathVariable Long franchiseId, @Valid @RequestBody CreateBranchRequest request) {
        BranchResponse response = branchService.create(franchiseId, request);
        return ResponseEntity.created(URI.create("/api/branches/" + response.id())).body(response);
    }

    @PatchMapping("/branches/{branchId}")
    @Operation(summary = "Actualizar nombre de sucursal")
    @ApiResponse(responseCode = "200", description = "Sucursal actualizada",
            content = @Content(schema = @Schema(implementation = BranchResponse.class)))
    @ApiResponse(responseCode = "404", description = "Sucursal no encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BranchResponse> updateName(
            @PathVariable Long branchId, @Valid @RequestBody UpdateBranchRequest request) {
        return ResponseEntity.ok(branchService.updateName(branchId, request));
    }
}
