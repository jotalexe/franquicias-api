package com.example.franquicias.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.franquicias.entity.Branch;
import com.example.franquicias.entity.Franchise;
import com.example.franquicias.entity.Product;
import com.example.franquicias.repository.BranchRepository;
import com.example.franquicias.repository.FranchiseRepository;
import com.example.franquicias.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Cubre los 8 escenarios del endpoint GET /api/franchises/{id}/products/max-stock
 * exigidos por el reto, usando una base PostgreSQL real (Testcontainers) para
 * que el ordenamiento y el desempate se validen contra SQL real y no contra
 * un mock que simplemente devolvería lo que se le programe.
 */
class MaxStockIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FranchiseRepository franchiseRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
        branchRepository.deleteAll();
        franchiseRepository.deleteAll();
    }

    @Test
    void scenario_multipleBranchesMultipleProducts_returnsTopProductPerBranch() throws Exception {
        Franchise franchise = franchiseRepository.save(Franchise.builder().name("Franquicia Principal").build());

        Branch medellin = branchRepository.save(Branch.builder().name("Medellin").franchise(franchise).build());
        Branch bogota = branchRepository.save(Branch.builder().name("Bogota").franchise(franchise).build());
        Branch cali = branchRepository.save(Branch.builder().name("Cali").franchise(franchise).build());

        saveProduct(medellin, "Producto A", 100);
        Product productB = saveProduct(medellin, "Producto B", 250);

        saveProduct(bogota, "Producto C", 80);
        Product productD = saveProduct(bogota, "Producto D", 400);

        Product productE = saveProduct(cali, "Producto E", 150);
        saveProduct(cali, "Producto F", 90);

        MvcResult result = mockMvc.perform(get("/api/franchises/" + franchise.getId() + "/products/max-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(findByBranch(body, "Medellin").get("productId").asLong()).isEqualTo(productB.getId());
        assertThat(findByBranch(body, "Medellin").get("stock").asInt()).isEqualTo(250);
        assertThat(findByBranch(body, "Bogota").get("productId").asLong()).isEqualTo(productD.getId());
        assertThat(findByBranch(body, "Bogota").get("stock").asInt()).isEqualTo(400);
        assertThat(findByBranch(body, "Cali").get("productId").asLong()).isEqualTo(productE.getId());
        assertThat(findByBranch(body, "Cali").get("stock").asInt()).isEqualTo(150);
    }

    @Test
    void scenario_branchWithoutProducts_isExcludedFromResult() throws Exception {
        Franchise franchise = franchiseRepository.save(Franchise.builder().name("Franquicia Vacia Parcial").build());
        Branch withProducts = branchRepository.save(Branch.builder().name("Con productos").franchise(franchise).build());
        branchRepository.save(Branch.builder().name("Sin productos").franchise(franchise).build());
        saveProduct(withProducts, "Unico producto", 10);

        mockMvc.perform(get("/api/franchises/" + franchise.getId() + "/products/max-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].branchName").value("Con productos"));
    }

    @Test
    void scenario_productsWithZeroStock_areStillConsidered() throws Exception {
        Franchise franchise = franchiseRepository.save(Franchise.builder().name("Franquicia Stock Cero").build());
        Branch branch = branchRepository.save(Branch.builder().name("Sucursal").franchise(franchise).build());
        Product zeroStock = saveProduct(branch, "Producto sin stock", 0);

        mockMvc.perform(get("/api/franchises/" + franchise.getId() + "/products/max-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productId").value(zeroStock.getId()))
                .andExpect(jsonPath("$[0].stock").value(0));
    }

    @Test
    void scenario_tiedStock_resolvesByLowestProductId() throws Exception {
        Franchise franchise = franchiseRepository.save(Franchise.builder().name("Franquicia Empate").build());
        Branch branch = branchRepository.save(Branch.builder().name("Sucursal").franchise(franchise).build());

        Product first = saveProduct(branch, "Producto Primero", 100);
        saveProduct(branch, "Producto Segundo", 100);

        mockMvc.perform(get("/api/franchises/" + franchise.getId() + "/products/max-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productId").value(first.getId()))
                .andExpect(jsonPath("$[0].productName").value("Producto Primero"));
    }

    @Test
    void scenario_nonExistentFranchise_returns404() throws Exception {
        mockMvc.perform(get("/api/franchises/987654/products/max-stock"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void scenario_franchiseWithoutBranches_returnsEmptyList() throws Exception {
        Franchise franchise = franchiseRepository.save(Franchise.builder().name("Franquicia Sin Sucursales").build());

        mockMvc.perform(get("/api/franchises/" + franchise.getId() + "/products/max-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private Product saveProduct(Branch branch, String name, int stock) {
        return productRepository.save(Product.builder().name(name).stock(stock).branch(branch).build());
    }

    private JsonNode findByBranch(JsonNode array, String branchName) {
        for (JsonNode node : array) {
            if (node.get("branchName").asText().equals(branchName)) {
                return node;
            }
        }
        throw new IllegalStateException("No se encontro la sucursal " + branchName + " en la respuesta: " + array);
    }
}
