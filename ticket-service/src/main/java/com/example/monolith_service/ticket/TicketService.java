package com.example.monolith_service.ticket;

import com.example.monolith_service.error.ResourceNotFoundException;
import com.example.monolith_service.ticket.dto.TicketPageResponse;
import com.example.monolith_service.ticket.dto.TicketRequest;
import com.example.monolith_service.ticket.dto.TicketResponse;
import com.example.monolith_service.ticket.dto.TicketSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class TicketService {

    private static final long DUE_SOON_SECONDS = 30 * 60;

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public TicketResponse create(TicketRequest request) {
        Ticket ticket = new Ticket();
        applyRequest(ticket, request);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setSlaDueAt(Instant.now().plus(Duration.ofMinutes(request.getSlaMinutes())));
        return toResponse(ticketRepository.save(ticket));
    }

    public TicketPageResponse getAll(
        int page,
        int size,
        String sortBy,
        String direction,
        String search,
        TicketStatus status,
        TicketPriority priority
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort sort = "desc".equalsIgnoreCase(direction)
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        String normalizedSearch = search == null ? "" : search.trim();

        Specification<Ticket> spec = (root, query, cb) -> cb.conjunction();
        if (!normalizedSearch.isEmpty()) {
            String like = "%" + normalizedSearch.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("customerName")), like),
                cb.like(cb.lower(root.get("customerEmail")), like)
            ));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (priority != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }

        Page<Ticket> ticketPage = ticketRepository.findAll(spec, pageable);

        List<TicketResponse> responses = ticketPage.getContent().stream().map(this::toResponse).toList();

        return new TicketPageResponse(
            responses,
            ticketPage.getNumber(),
            ticketPage.getSize(),
            ticketPage.getTotalElements(),
            ticketPage.getTotalPages(),
            ticketPage.hasNext(),
            ticketPage.hasPrevious()
        );
    }

    public TicketResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public TicketResponse update(Long id, TicketRequest request) {
        Ticket ticket = findOrThrow(id);
        applyRequest(ticket, request);
        ticket.setSlaDueAt(Instant.now().plus(Duration.ofMinutes(request.getSlaMinutes())));
        return toResponse(ticketRepository.save(ticket));
    }

    public TicketResponse updateStatus(Long id, TicketStatus status) {
        Ticket ticket = findOrThrow(id);
        ticket.setStatus(status);
        return toResponse(ticketRepository.save(ticket));
    }

    public TicketResponse assign(Long id, String assignedTo) {
        Ticket ticket = findOrThrow(id);
        ticket.setAssignedTo(assignedTo.trim());
        return toResponse(ticketRepository.save(ticket));
    }

    public void delete(Long id) {
        ticketRepository.delete(findOrThrow(id));
    }

    public TicketSummaryResponse summary() {
        List<Ticket> tickets = ticketRepository.findAll();
        Instant now = Instant.now();

        long total = tickets.size();
        long open = tickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long inProgress = tickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long blocked = tickets.stream().filter(t -> t.getStatus() == TicketStatus.BLOCKED).count();
        long resolved = tickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
        long breached = tickets.stream().filter(t -> t.getStatus() != TicketStatus.RESOLVED && t.getSlaDueAt().isBefore(now)).count();
        long dueSoon = tickets.stream()
            .filter(t -> t.getStatus() != TicketStatus.RESOLVED)
            .map(t -> Duration.between(now, t.getSlaDueAt()).getSeconds())
            .filter(sec -> sec >= 0 && sec <= DUE_SOON_SECONDS)
            .count();

        return new TicketSummaryResponse(total, open, inProgress, blocked, resolved, breached, dueSoon);
    }

    private Ticket findOrThrow(Long id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    private void applyRequest(Ticket ticket, TicketRequest request) {
        ticket.setTitle(request.getTitle().trim());
        ticket.setDescription(request.getDescription().trim());
        ticket.setCustomerName(request.getCustomerName().trim());
        ticket.setCustomerEmail(request.getCustomerEmail().trim().toLowerCase());
        ticket.setPriority(request.getPriority());
        String assignee = request.getAssignedTo();
        ticket.setAssignedTo(assignee == null || assignee.isBlank() ? null : assignee.trim());
    }

    private TicketResponse toResponse(Ticket ticket) {
        long remaining = Duration.between(Instant.now(), ticket.getSlaDueAt()).getSeconds();
        return new TicketResponse(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getCustomerName(),
            ticket.getCustomerEmail(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.getAssignedTo(),
            ticket.getSlaDueAt(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt(),
            remaining,
            remaining < 0 && ticket.getStatus() != TicketStatus.RESOLVED
        );
    }
}
