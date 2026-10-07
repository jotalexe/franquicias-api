package com.example.franquicias.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.franquicias.dto.ProductResponse;
import com.example.franquicias.exception.ResourceNotFoundException;
import com.example.franquicias.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void create_withValidPayload_returns201() throws Exception {
        when(productService.create(eq(10L), any())).thenReturn(new ProductResponse(100L, "Producto A", 100, 10L));

        mockMvc.perform(post("/api/branches/10/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Producto A\",\"stock\":100}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.stock").value(100));
    }

    @Test
    void create_whenBranchDoesNotExist_returns404() throws Exception {
        when(productService.create(eq(99L), any())).thenThrow(ResourceNotFoundException.branch(99L));

        mockMvc.perform(post("/api/branches/99/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Producto A\",\"stock\":100}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withNegativeStock_returns400() throws Exception {
        mockMvc.perform(post("/api/branches/10/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Producto A\",\"stock\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_withMissingStock_returns400() throws Exception {
        mockMvc.perform(post("/api/branches/10/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Producto A\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withBlankName_returns400() throws Exception {
        mockMvc.perform(post("/api/branches/10/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"stock\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_whenProductExistsInBranch_returns204() throws Exception {
        doNothing().when(productService).delete(10L, 100L);

        mockMvc.perform(delete("/api/branches/10/products/100"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenProductNotInBranch_returns404() throws Exception {
        doThrow(ResourceNotFoundException.productInBranch(100L, 20L))
                .when(productService).delete(20L, 100L);

        mockMvc.perform(delete("/api/branches/20/products/100"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStock_withValidValue_returns200() throws Exception {
        when(productService.updateStock(eq(100L), any())).thenReturn(new ProductResponse(100L, "Producto A", 250, 10L));

        mockMvc.perform(patch("/api/products/100/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":250}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(250));
    }

    @Test
    void updateStock_withNegativeValue_returns400() throws Exception {
        mockMvc.perform(patch("/api/products/100/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStock_whenProductDoesNotExist_returns404() throws Exception {
        when(productService.updateStock(eq(999L), any())).thenThrow(ResourceNotFoundException.product(999L));

        mockMvc.perform(patch("/api/products/999/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":10}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateName_withValidPayload_returns200() throws Exception {
        when(productService.updateName(eq(100L), any())).thenReturn(new ProductResponse(100L, "Nuevo producto", 100, 10L));

        mockMvc.perform(patch("/api/products/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nuevo producto\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nuevo producto"));
    }
}
