package com.example.franquicias.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Patron "singleton container" de Testcontainers: el contenedor se arranca
 * una sola vez en un bloque estatico y NUNCA se detiene explicitamente (lo
 * limpia el Ryuk reaper al terminar la JVM). No se usa @Container/@Testcontainers
 * porque esa extension gestiona el ciclo de vida por clase de test; con un
 * campo estatico heredado, detendria el contenedor al terminar la primera
 * clase de test y rompería las siguientes (sintoma observado: "Connection
 * refused" en la segunda clase de integracion que se ejecuta).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }
}
