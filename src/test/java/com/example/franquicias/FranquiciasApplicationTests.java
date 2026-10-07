package com.example.franquicias;

import com.example.franquicias.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Verifica que el contexto de Spring completo levanta correctamente: todos
 * los beans se resuelven sin ciclos, Flyway aplica las migraciones contra
 * Postgres real y la configuracion de cada capa es consistente.
 */
class FranquiciasApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}
