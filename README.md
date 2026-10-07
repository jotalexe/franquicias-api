# Franchise API

API REST para gestionar franquicias, sus sucursales y los productos que cada sucursal tiene en stock. Resuelve, entre otras cosas, la consulta de "cual es el producto con mayor stock de cada sucursal de una franquicia", que es el caso de uso principal del reto.

## Arquitectura

Arquitectura en capas (no hexagonal ni DDD: el dominio tiene 3 entidades con relaciones simples, y una arquitectura mas pesada seria sobreingenieria para este alcance).

```
Cliente HTTP
    |
    v
controller   -> recibe el request, valida (@Valid) y delega. Sin logica de negocio.
    |
    v
service      -> logica de negocio, orquestacion, transacciones.
    |
    v
repository   -> Spring Data JPA.
    |
    v
PostgreSQL
```

```
src/main/java/com/example/franquicias
├── controller    FranchiseController, BranchController, ProductController
├── service       interfaces + service/impl (implementaciones)
├── repository    FranchiseRepository, BranchRepository, ProductRepository
├── entity        Franchise, Branch, Product
├── dto           records de request/response
├── mapper        MapStruct (entity <-> dto)
├── exception     ResourceNotFoundException + GlobalExceptionHandler
├── config        OpenApiConfig
└── FranquiciasApplication.java
```

Las entidades usan relaciones **unidireccionales** (`Branch -> Franchise`, `Product -> Branch`, solo el lado FK). No se mapean colecciones inversas (`Franchise.branches`, `Branch.products`) para evitar los problemas clasicos de colecciones bidireccionales con Lombok/JPA (equals/hashCode recursivos, N+1, serializacion). Las consultas "todas las sucursales de una franquicia" se resuelven con queries de repository (`findByFranchiseId`).

`BranchService` y `ProductService` validan la existencia del recurso padre contra el **repository del padre** (no contra su service). Esto es deliberado: `FranchiseService` orquesta `BranchService` y `ProductService` para resolver el endpoint de max-stock, así que si `BranchService` dependiera de `FranchiseService` se formaría un ciclo de beans que Spring no puede resolver por inyección de constructor.

## Tecnologias

| Capa | Tecnologia |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.16 |
| Build | Maven (con wrapper `mvnw`, no requiere Maven instalado) |
| Persistencia | Spring Data JPA + PostgreSQL 16 |
| Migraciones | Flyway |
| Validacion | Jakarta Bean Validation |
| Documentacion | springdoc-openapi / Swagger UI |
| Mapeo DTO <-> entidad | MapStruct |
| Tests | JUnit 5, Mockito, MockMvc, Testcontainers |
| Contenedores | Docker, Docker Compose |
| IaC (opcional) | Terraform (AWS App Runner + RDS) |

## Requisitos

- Java 21+ (para ejecutar localmente sin Docker)
- Docker + Docker Compose (recomendado; no requiere tener Java ni Maven instalados)
- No se requiere Maven instalado: el proyecto incluye el wrapper (`mvnw` / `mvnw.cmd`)

## Instalacion y ejecucion local

### Opcion A: Docker Compose (recomendada)

Levanta la API y PostgreSQL con un solo comando:

```bash
docker compose up --build
```

La API queda disponible en `http://localhost:8080`. Para detener:

```bash
docker compose down
```

Para borrar tambien el volumen de datos de Postgres:

```bash
docker compose down -v
```

### Opcion B: Maven wrapper + Postgres local

1. Levantar solo la base de datos:
   ```bash
   docker compose up -d postgres
   ```
2. Ejecutar la aplicacion:
   ```bash
   ./mvnw spring-boot:run
   ```
   (en Windows: `mvnw.cmd spring-boot:run`)

Por defecto usa el perfil `dev` (`application-dev.yml`), que apunta a `localhost:5432` con las credenciales del `.env.example`.

## Configuracion

La configuracion sensible se maneja 100% por variables de entorno (nunca hardcodeada). Copiar `.env.example` a `.env` y ajustar si es necesario:

| Variable | Descripcion | Default (dev) |
|---|---|---|
| `DATABASE_URL` | JDBC URL de PostgreSQL | `jdbc:postgresql://localhost:5433/franchise_db` |
| `DB_HOST_PORT` | Puerto expuesto al host por docker-compose para Postgres (no afecta la comunicacion interna API -> DB) | `5433` |
| `DATABASE_USERNAME` | Usuario de la base de datos | `franchise_user` |
| `DATABASE_PASSWORD` | Password de la base de datos | `franchise_pass` |
| `SERVER_PORT` | Puerto HTTP de la API | `8080` |
| `SPRING_PROFILES_ACTIVE` | Perfil activo (`dev` o `test`) | `dev` |

Perfiles: `application.yml` (comun), `application-dev.yml` (Postgres real, local o cloud), `application-test.yml` (usado en los tests de integracion con Testcontainers).

## Tests

```bash
./mvnw test
```

59 tests, 0 fallos:

- **Unitarios** (Mockito, sin contexto Spring): logica de orquestacion de cada service — `FranchiseServiceImplTest`, `BranchServiceImplTest`, `ProductServiceImplTest`.
- **Slice de controller** (`@WebMvcTest` + MockMvc, service mockeado): contrato HTTP, validacion de Bean Validation, mapeo de excepciones a status codes — `FranchiseControllerTest`, `BranchControllerTest`, `ProductControllerTest`.
- **Integracion** (`@SpringBootTest` + Testcontainers con PostgreSQL real): flujo completo create -> update -> delete y errores de negocio (`FranchiseLifecycleIntegrationTest`), y los 8 escenarios del endpoint de max-stock (`MaxStockIntegrationTest`).

Los tests de integracion requieren Docker activo (Testcontainers levanta un contenedor de Postgres real; el ordenamiento y el desempate del endpoint de max-stock se validan contra SQL real, no contra un mock).

## Swagger / OpenAPI

Con la aplicacion corriendo:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Spec OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

## Endpoints

| Metodo | Path | Descripcion |
|---|---|---|
| POST | `/api/franchises` | Crear franquicia |
| PATCH | `/api/franchises/{franchiseId}` | Renombrar franquicia |
| POST | `/api/franchises/{franchiseId}/branches` | Crear sucursal |
| PATCH | `/api/branches/{branchId}` | Renombrar sucursal |
| POST | `/api/branches/{branchId}/products` | Crear producto |
| DELETE | `/api/branches/{branchId}/products/{productId}` | Eliminar producto |
| PATCH | `/api/products/{productId}/stock` | Actualizar stock |
| PATCH | `/api/products/{productId}` | Renombrar producto |
| GET | `/api/franchises/{franchiseId}/products/max-stock` | Producto con mayor stock por sucursal |

> Se implementan exactamente los 9 endpoints que exige el reto (ver seccion "Decisiones tecnicas" sobre por que no se agregaron endpoints de lectura adicionales).

## Postman

Coleccion en [`postman/franquicias-api.postman_collection.json`](postman/franquicias-api.postman_collection.json), con escenarios positivos y negativos (IDs inexistentes, campos vacios, stock negativo, JSON invalido, producto de otra sucursal). Los requests encadenan IDs via variables de coleccion, asi que debe ejecutarse con el Collection Runner en orden, contra una instancia limpia o existente de la API.

## Docker

```bash
docker compose up --build     # construir y levantar API + Postgres
docker compose up -d          # levantar en segundo plano
docker compose logs -f api    # ver logs de la API
docker compose down           # detener y eliminar contenedores
docker compose down -v        # detener y tambien borrar el volumen de datos
```

El `Dockerfile` es multi-stage: build con el wrapper de Maven sobre `eclipse-temurin:21-jdk-alpine`, runtime sobre `eclipse-temurin:21-jre-alpine` con un usuario no-root.

## Base de datos

PostgreSQL 16, expuesto en el host en el puerto **5433** (no el 5432 por defecto, para no chocar con un Postgres local que ya pueda estar corriendo; configurable con `DB_HOST_PORT`). La comunicacion interna entre el contenedor de la API y el de la base de datos siempre usa el puerto 5432 de la red de Docker, sin importar el valor de `DB_HOST_PORT`. El esquema se crea y versiona con **Flyway** (`src/main/resources/db/migration/V1__init_schema.sql`), no con `ddl-auto` de Hibernate (que queda en `validate`, nunca en `update`/`create`). El stock de un producto tiene un `CHECK (stock >= 0)` a nivel de base de datos ademas de la validacion en el DTO, como defensa en profundidad.

## Cloud

Preparado para desplegar en AWS sin ejecutar nada automaticamente (ver seccion 17/16 del reto: no se corren comandos que generen costo). Propuesta economica:

- **ECR**: registro de la imagen Docker.
- **App Runner**: ejecuta el contenedor; escala a demanda, sin servidores que administrar, es la opcion mas barata para un servicio de bajo trafico.
- **RDS PostgreSQL** (`db.t4g.micro`), sin acceso publico. App Runner llega a RDS mediante un **VPC Connector** (trafico solo de entrada a la VPC), evitando el costo de un NAT Gateway.
- **Secrets Manager** para el password de la base de datos (nunca en variables de entorno planas ni en el repo).

Infraestructura como codigo en [`infrastructure/`](infrastructure/) (`main.tf`, `variables.tf`, `outputs.tf`). Para desplegar (no ejecutado por este asistente, bajo pedido explicito de no generar costos):

```bash
cd infrastructure
terraform init
terraform plan -var="db_password=<password-seguro>"
terraform apply -var="db_password=<password-seguro>"
```

Y luego publicar la imagen en el repositorio ECR que crea Terraform (`terraform output ecr_repository_url`) con `docker push`. Para destruir todo sin afectar otros recursos de la cuenta: `terraform destroy`.

## Decisiones tecnicas

**Base de datos — PostgreSQL.** La jerarquia franquicia -> sucursal -> producto es relacional por naturaleza, y el endpoint principal (max stock por sucursal) es una agregacion (`MAX(stock) GROUP BY branch`) trivial en SQL e incomoda de resolver limpiamente en Mongo/DynamoDB sin pipelines adicionales. Redis queda descartado por no ser una persistencia primaria adecuada para este modelo.

**Arquitectura — capas, no hexagonal/DDD.** El dominio es simple (3 entidades, relaciones claras); una arquitectura mas pesada agregaria indirecta sin beneficio real para este alcance.

**Spring Boot 3.5.16.** El reto pide explicitamente "Spring Boot 3.x". Al momento de construir este proyecto la rama 3.x completa de Spring Boot ya esta en end-of-life (la 3.5 fue la ultima, con soporte OSS hasta el 30 de junio de 2026) y las herramientas oficiales (start.spring.io) ya solo generan proyectos 4.x. Se mantiene 3.5.16 (la ultima patch de la ultima rama 3.x) por ser el requisito explicito del reto; los artefactos siguen disponibles en Maven Central y el build es reproducible.

**Consulta de max-stock.** Se resuelve con el metodo derivado `ProductRepository.findTopByBranchIdOrderByStockDescIdAsc(branchId)`: una query por sucursal, ordenada por stock descendente y, en caso de empate, por id ascendente. `FranchiseServiceImpl` valida que la franquicia exista, obtiene sus sucursales y para cada una pide su producto top, descartando (via `Optional`) las sucursales sin productos. Comportamiento documentado explicitamente (tambien en Swagger):
- Sucursal sin productos -> se **excluye** del resultado (no hay un producto que reportar).
- Empate de stock -> gana el producto con **menor id** (orden de creacion), de forma deterministica.
- Franquicia sin sucursales -> responde `200` con lista vacia.
- Franquicia inexistente -> `404`.

**Manejo de errores.** `@RestControllerAdvice` centralizado (`GlobalExceptionHandler`) que traduce excepciones a un `ErrorResponse` consistente (`timestamp`, `status`, `error`, `message`, `path`) y nunca expone stack traces al cliente; el detalle completo queda en el log del servidor. `ResourceNotFoundException` -> 404; errores de Bean Validation, JSON malformado o tipos de parametro invalidos -> 400; cualquier otra excepcion no prevista -> 500 generico.

**Validaciones.** Jakarta Bean Validation sobre los DTOs de request (records inmutables): `@NotBlank` en nombres, `@NotNull` + `@Min(0)` en stock. El stock nunca puede ser negativo, reforzado tambien con un `CHECK` en la base de datos.

**Alcance de los endpoints.** Se implementan exactamente los 9 endpoints que pide el reto (5 obligatorios + 3 PATCH + el query de max-stock). Deliberadamente no se agregaron endpoints de lectura (`GET` por id, listados) que no fueron solicitados, para no introducir alcance no pedido.

**409 Conflict.** El enunciado pide usar los codigos HTTP "correctamente", incluyendo 409, pero ninguna regla de negocio especificada (no hay restriccion de unicidad de nombres, por ejemplo) genera naturalmente un conflicto. No se forzo un caso artificial solo para marcar la casilla; `GlobalExceptionHandler` esta listo para mapear una futura excepcion de conflicto de negocio a 409 si se agrega esa regla.

## Pendientes / fuera de alcance

- No se desplego realmente en AWS (Terraform queda listo pero sin aplicar, segun lo pedido).
- No hay autenticacion/autorizacion (el reto no la exige; ver seccion de seguridad mas abajo).
- No hay endpoints de lectura/listado mas alla de los 9 solicitados.

## Seguridad (revision basica)

- Todas las entradas pasan por Bean Validation antes de llegar a la capa de negocio.
- JPA/Hibernate con parametros preparados en todas las queries (sin concatenacion de SQL) — sin superficie de inyeccion SQL.
- Ninguna credencial esta hardcodeada; todo viene de variables de entorno (`.env`, nunca commiteado) o, en la propuesta de Cloud, de AWS Secrets Manager.
- Las respuestas de error nunca incluyen stack traces ni detalles internos.
- No se agrego Spring Security: el reto no exige autenticacion y agregarla habria introducido complejidad sin valor para este alcance.
