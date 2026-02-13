# SLA Queue Monitor - Microservices

This repository now contains a **microservices split** of the original monolith.

## Services

- `ticket-service` (port `8081` by default)
  - Owns ticket domain and SLA queue APIs
  - Serves frontend pages (`/login.html`, `/index.html`)
  - Endpoints: `/tickets/**`, `/health`

- `assistant-service` (port `8082` by default)
  - Owns AI chat endpoint
  - Reads ticket data from `ticket-service` over HTTP
  - Endpoint: `/assistant/chat`

## Architecture

- Frontend is hosted by `ticket-service`
- Frontend calls AI service at `http://localhost:8082/assistant/chat`
- `assistant-service` calls `ticket-service` at `http://localhost:8081/tickets`

## Run

Open two terminals.

### 1) Start ticket-service

```powershell
cd ticket-service
.\mvnw.cmd spring-boot:run
```

### 2) Start assistant-service

```powershell
cd assistant-service
.\mvnw.cmd spring-boot:run
```

## Access

- UI: `http://localhost:8081/login.html`
- Ticket API summary: `http://localhost:8081/tickets/summary`
- AI chat API: `http://localhost:8082/assistant/chat`

## Login

- Email: `sgaddam@ops.com`
- Password: `Skumar@2000`

## Useful env vars

### ticket-service
- `SERVER_PORT` (default `8081`)

### assistant-service
- `SERVER_PORT` (default `8082`)
- `TICKET_SERVICE_BASE_URL` (default `http://localhost:8081`)
