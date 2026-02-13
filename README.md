# Monolithic Service

Spring Boot monolith for **real-time support queue and SLA monitoring**.

## Stack
- Java 17+
- Spring Boot 4.0.2
- Spring Web MVC
- Spring Data JPA
- H2 (dev) / PostgreSQL (prod)
- Flyway
- Static HTML/CSS/JS frontend

## What It Solves
- Tracks incoming incidents/tickets in one queue
- Monitors SLA in real time (countdown, due soon, breached)
- Enables fast operations (status transitions, assignment, escalations)
- Provides AI triage assistant for summary and next-best action

## Features
- Ticket CRUD
- Status workflow: `OPEN`, `IN_PROGRESS`, `BLOCKED`, `RESOLVED`
- Priority levels: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- Assignment workflow
- Queue filters/search/sort/pagination
- SLA summary metrics endpoint
- Real-time frontend countdown and breach highlighting
- Bulk escalate breached tickets (UI action)
- AI assistant endpoint for triage insights

## API
Base URL: `http://localhost:8080`

- `POST /tickets`
- `GET /tickets`
- `GET /tickets/{id}`
- `PUT /tickets/{id}`
- `PATCH /tickets/{id}/status`
- `PATCH /tickets/{id}/assign`
- `GET /tickets/summary`
- `DELETE /tickets/{id}`
- `POST /assistant/chat`

### `GET /tickets` query params
- `page` (default `0`)
- `size` (default `10`)
- `sortBy` (`id`, `title`, `priority`, `status`, `slaDueAt`, `createdAt`)
- `direction` (`asc`, `desc`)
- `search` (title/customer/email)
- `status` (optional)
- `priority` (optional)

## Sample Ticket Create
```json
{
  "title": "Checkout fails for VISA cards",
  "description": "Customers see timeout while paying with VISA",
  "customerName": "Ava Miles",
  "customerEmail": "ava@example.com",
  "priority": "CRITICAL",
  "assignedTo": "Rohit",
  "slaMinutes": 15
}
```

## Run
```powershell
cd monolith-service
.\mvnw.cmd spring-boot:run
```

To run on port 8081:
```powershell
$env:SERVER_PORT="8081"
.\mvnw.cmd spring-boot:run
```

## Login (UI)
- URL: `http://localhost:8080/login.html`
- Email: `sgaddam@ops.com`
- Password: `Skumar@2000`

## Notes
- Dev profile uses H2 in-memory DB and auto schema update for fast local iteration.
- Migration script is in `monolith-service/src/main/resources/db/migration/V1__create_tickets_table.sql`.
