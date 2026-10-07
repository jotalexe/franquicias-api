package com.example.franquicias.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.franquicias.dto.FranchiseResponse;
import com.example.franquicias.dto.MaxStockProductResponse;
import com.example.franquicias.exception.ResourceNotFoundException;
import com.example.franquicias.service.FranchiseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FranchiseController.class)
class FranchiseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FranchiseService franchiseService;

    @Test
    void create_withValidName_returns201() throws Exception {
        when(franchiseService.create(any())).thenReturn(new FranchiseResponse(1L, "Franquicia Principal"));

        mockMvc.perform(post("/api/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Franquicia Principal\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Franquicia Principal"));
    }

    @Test
    void create_withBlankName_returns400() throws Exception {
        mockMvc.perform(post("/api/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_withMissingNameField_returns400() throws Exception {
        mockMvc.perform(post("/api/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateName_whenFranchiseExists_returns200() throws Exception {
        when(franchiseService.updateName(eq(1L), any())).thenReturn(new FranchiseResponse(1L, "Nuevo nombre"));

        mockMvc.perform(patch("/api/franchises/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nuevo nombre\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nuevo nombre"));
    }

    @Test
    void updateName_whenFranchiseDoesNotExist_returns404() throws Exception {
        when(franchiseService.updateName(eq(99L), any())).thenThrow(ResourceNotFoundException.franchise(99L));

        mockMvc.perform(patch("/api/franchises/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nuevo nombre\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getMaxStockProducts_returnsListFromService() throws Exception {
        when(franchiseService.getMaxStockProductsByBranch(1L)).thenReturn(List.of(
                new MaxStockProductResponse(10L, "Medellin", 20L, "Producto B", 250)));

        mockMvc.perform(get("/api/franchises/1/products/max-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].branchName").value("Medellin"))
                .andExpect(jsonPath("$[0].stock").value(250));
    }

    @Test
    void getMaxStockProducts_whenFranchiseDoesNotExist_returns404() throws Exception {
        when(franchiseService.getMaxStockProductsByBranch(99L)).thenThrow(ResourceNotFoundException.franchise(99L));

        mockMvc.perform(get("/api/franchises/99/products/max-stock"))
                .andExpect(status().isNotFound());
    }
}
