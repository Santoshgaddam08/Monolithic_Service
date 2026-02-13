package com.example.monolith_service.ticket;

import com.example.monolith_service.ticket.dto.TicketAssignRequest;
import com.example.monolith_service.ticket.dto.TicketPageResponse;
import com.example.monolith_service.ticket.dto.TicketRequest;
import com.example.monolith_service.ticket.dto.TicketResponse;
import com.example.monolith_service.ticket.dto.TicketStatusRequest;
import com.example.monolith_service.ticket.dto.TicketSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@Valid @RequestBody TicketRequest request) {
        return ticketService.create(request);
    }

    @GetMapping
    public TicketPageResponse getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String direction,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) TicketStatus status,
        @RequestParam(required = false) TicketPriority priority
    ) {
        Set<String> allowedSortFields = Set.of("id", "title", "priority", "status", "slaDueAt", "createdAt");
        String safeSortBy = allowedSortFields.contains(sortBy) ? sortBy : "id";
        String safeDirection = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";
        return ticketService.getAll(page, size, safeSortBy, safeDirection, search, status, priority);
    }

    @GetMapping("/{id}")
    public TicketResponse getById(@PathVariable Long id) {
        return ticketService.getById(id);
    }

    @PutMapping("/{id}")
    public TicketResponse update(@PathVariable Long id, @Valid @RequestBody TicketRequest request) {
        return ticketService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public TicketResponse updateStatus(@PathVariable Long id, @Valid @RequestBody TicketStatusRequest request) {
        return ticketService.updateStatus(id, request.getStatus());
    }

    @PatchMapping("/{id}/assign")
    public TicketResponse assign(@PathVariable Long id, @Valid @RequestBody TicketAssignRequest request) {
        return ticketService.assign(id, request.getAssignedTo());
    }

    @GetMapping("/summary")
    public TicketSummaryResponse summary() {
        return ticketService.summary();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ticketService.delete(id);
    }
}
