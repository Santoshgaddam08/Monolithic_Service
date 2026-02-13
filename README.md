# Monolithic Service

Spring Boot monolithic backend with Employee CRUD APIs, profile-based database configuration, Flyway migrations, and static UI pages.

## Tech Stack
- Java 17
- Spring Boot 4.0.2
- Spring Web MVC
- Spring Data JPA
- Flyway
- H2 (dev)
- PostgreSQL (prod)
- springdoc OpenAPI (Swagger UI)

## Project Structure
- `monolith-service/src/main/java/com/example/monolith_service/employee` - Employee domain (controller, service, repository, DTOs)
- `monolith-service/src/main/java/com/example/monolith_service/error` - Global exception handling
- `monolith-service/src/main/resources/db/migration` - Flyway SQL migrations
- `monolith-service/src/main/resources/static` - Static pages (`login.html`, `index.html`)
- `monolith-service/src/main/resources/application*.properties` - Profile-based configuration

## Features
- Employee CRUD APIs
- Pagination, sorting, and search on employee listing
- Input validation (`@Valid` with clear validation messages)
- Global exception handling for not found and validation errors
- Flyway-managed schema migrations
- Environment profiles:
  - `dev` (default): in-memory H2
  - `prod`: PostgreSQL via environment variables

## API Endpoints
Base URL: `http://localhost:8080`

- `GET /` -> redirects to `/login.html`
- `GET /health` -> returns `OK`
- `POST /employees` -> create employee
- `GET /employees` -> list employees (pagination/sort/search)
- `GET /employees/{id}` -> get employee by id
- `PUT /employees/{id}` -> update employee
- `DELETE /employees/{id}` -> delete employee

### Query Parameters for `GET /employees`
- `page` (default: `0`)
- `size` (default: `10`, max: `100`)
- `sortBy` (`id`, `firstName`, `lastName`, `email`; default: `id`)
- `direction` (`asc` or `desc`; default: `asc`)
- `search` (optional text search on firstName/lastName/email)

## Request Example
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

## Run Locally
From repo root:

```bash
cd monolith-service
./mvnw spring-boot:run
```

For Windows PowerShell:

```powershell
cd monolith-service
.\mvnw.cmd spring-boot:run
```

Application starts on `http://localhost:8080`.

## Profile Configuration
Default profile is `dev` (`application.properties`):
- H2 in-memory DB
- H2 console enabled at `/h2-console`

Run with `prod` profile:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:postgresql://localhost:5432/monolithdb"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
.\mvnw.cmd spring-boot:run
```

## API Documentation
After startup, open:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Quick cURL Commands
Create employee:

```bash
curl -X POST http://localhost:8080/employees \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com"}'
```

List employees:

```bash
curl "http://localhost:8080/employees?page=0&size=10&sortBy=id&direction=asc&search="
```

Get by id:

```bash
curl http://localhost:8080/employees/1
```

Update:

```bash
curl -X PUT http://localhost:8080/employees/1 \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jane.doe@example.com"}'
```

Delete:

```bash
curl -X DELETE http://localhost:8080/employees/1
```

## Notes
- Flyway runs automatically on startup and applies SQL scripts from `db/migration`.
- Runtime log/error files are intentionally not part of source code and should be gitignored.
