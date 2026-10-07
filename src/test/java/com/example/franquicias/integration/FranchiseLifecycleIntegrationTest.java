package com.example.franquicias.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Cubre el flujo completo end-to-end contra una base PostgreSQL real
 * (Testcontainers): crear franquicia -> sucursal -> producto -> actualizar
 * -> eliminar, junto con los escenarios negativos exigidos por el reto.
 */
class FranchiseLifecycleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullLifecycle_createUpdateDeleteAcrossFranchiseBranchProduct() throws Exception {
        long franchiseId = createFranchise("Franquicia Lifecycle");
        long branchId = createBranch(franchiseId, "Sucursal Centro");
        long productId = createProduct(branchId, "Producto Inicial", 50);

        mockMvc.perform(patch("/api/franchises/" + franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Franquicia Renombrada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Franquicia Renombrada"));

        mockMvc.perform(patch("/api/branches/" + branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sucursal Renombrada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sucursal Renombrada"));

        mockMvc.perform(patch("/api/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Producto Renombrado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Producto Renombrado"));

        mockMvc.perform(patch("/api/products/" + productId + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":300}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(300));

        mockMvc.perform(delete("/api/branches/" + branchId + "/products/" + productId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/branches/" + branchId + "/products/" + productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBranch_forNonExistentFranchise_returns404() throws Exception {
        mockMvc.perform(post("/api/franchises/999999/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sucursal Fantasma\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/franchises/999999/branches"));
    }

    @Test
    void createProduct_withNegativeStock_returns400() throws Exception {
        long franchiseId = createFranchise("Franquicia Stock Negativo");
        long branchId = createBranch(franchiseId, "Sucursal");

        mockMvc.perform(post("/api/branches/" + branchId + "/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Producto\",\"stock\":-10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_forNonExistentBranch_returns404() throws Exception {
        mockMvc.perform(post("/api/branches/999999/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Producto\",\"stock\":10}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_belongingToAnotherBranch_returns404() throws Exception {
        long franchiseId = createFranchise("Franquicia Cruzada");
        long branchOneId = createBranch(franchiseId, "Sucursal 1");
        long branchTwoId = createBranch(franchiseId, "Sucursal 2");
        long productId = createProduct(branchOneId, "Producto", 10);

        mockMvc.perform(delete("/api/branches/" + branchTwoId + "/products/" + productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRequest_withMalformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStock_withNegativeValue_returns400() throws Exception {
        long franchiseId = createFranchise("Franquicia Update Stock");
        long branchId = createBranch(franchiseId, "Sucursal");
        long productId = createProduct(branchId, "Producto", 10);

        mockMvc.perform(patch("/api/products/" + productId + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private long createFranchise(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return extractId(result);
    }

    private long createBranch(long franchiseId, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/franchises/" + franchiseId + "/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return extractId(result);
    }

    private long createProduct(long branchId, String name, int stock) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/branches/" + branchId + "/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"stock\":" + stock + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        return extractId(result);
    }

    private long extractId(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }
}
