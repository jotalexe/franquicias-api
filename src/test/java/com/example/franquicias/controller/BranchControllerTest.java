package com.example.franquicias.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.franquicias.dto.BranchResponse;
import com.example.franquicias.exception.ResourceNotFoundException;
import com.example.franquicias.service.BranchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BranchController.class)
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BranchService branchService;

    @Test
    void create_whenFranchiseExists_returns201() throws Exception {
        when(branchService.create(eq(1L), any())).thenReturn(new BranchResponse(10L, "Medellin", 1L));

        mockMvc.perform(post("/api/franchises/1/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Medellin\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.franchiseId").value(1));
    }

    @Test
    void create_whenFranchiseDoesNotExist_returns404() throws Exception {
        when(branchService.create(eq(99L), any())).thenThrow(ResourceNotFoundException.franchise(99L));

        mockMvc.perform(post("/api/franchises/99/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Medellin\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_withBlankName_returns400() throws Exception {
        mockMvc.perform(post("/api/franchises/1/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateName_whenBranchExists_returns200() throws Exception {
        when(branchService.updateName(eq(10L), any())).thenReturn(new BranchResponse(10L, "Nueva sucursal", 1L));

        mockMvc.perform(patch("/api/branches/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nueva sucursal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nueva sucursal"));
    }

    @Test
    void updateName_whenBranchDoesNotExist_returns404() throws Exception {
        when(branchService.updateName(eq(99L), any())).thenThrow(ResourceNotFoundException.branch(99L));

        mockMvc.perform(patch("/api/branches/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nueva sucursal\"}"))
                .andExpect(status().isNotFound());
    }
}
