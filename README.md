# Monolithic Service

Spring Boot monolith with Product Catalog and Inventory APIs, Flyway migrations, and static UI pages.

## Tech Stack
- Java 17
- Spring Boot 4.0.2
- Spring Web MVC
- Spring Data JPA
- Flyway
- H2 (dev)
- PostgreSQL (prod)
- springdoc OpenAPI (Swagger UI)

## Features
- Product CRUD APIs
- Inventory adjustment endpoint
- Pagination, sorting, search, and CSV export from the dashboard
- Dashboard KPIs (total products, low stock, out-of-stock, inventory value)
- Bulk restock and demo data seed actions
- AI Inventory Copilot chat (local backend endpoint)
- Request validation and global exception handling
- Flyway-managed schema
- Profiles:
  - `dev` (default): H2 in-memory
  - `prod`: PostgreSQL via env vars

## Endpoints
Base URL: `http://localhost:8080`

- `GET /` -> redirects to `/login.html`
- `GET /health` -> `OK`
- `POST /products`
- `GET /products`
- `GET /products/{id}`
- `PUT /products/{id}`
- `PATCH /products/{id}/inventory` (body: `{ "delta": 5 }` or negative)
- `DELETE /products/{id}`
- `POST /assistant/chat` (body: `{ "message": "Give me a summary" }`)

## `GET /products` Query Params
- `page` (default `0`)
- `size` (default `10`, max `100`)
- `sortBy` (`id`, `name`, `sku`, `price`, `quantity`)
- `direction` (`asc`, `desc`)
- `search` (optional: name or SKU)

## Product Request Example
```json
{
  "name": "Wireless Mouse",
  "sku": "MOUSE-WL-001",
  "price": 25.99,
  "quantity": 50
}
```

## Run Locally
```powershell
cd monolith-service
.\mvnw.cmd spring-boot:run
```

App URL: `http://localhost:8080`

## Production Profile Example
```powershell
cd monolith-service
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:postgresql://localhost:5432/monolithdb"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
.\mvnw.cmd spring-boot:run
```

## API Docs
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Notes
- Flyway migrations are in `monolith-service/src/main/resources/db/migration`.
- Local runtime logs should be gitignored.
