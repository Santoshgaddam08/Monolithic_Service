package com.example.monolith_service.ticket.dto;

import com.example.monolith_service.ticket.TicketPriority;
import com.example.monolith_service.ticket.TicketStatus;

import java.time.Instant;

public class TicketResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String customerName;
    private final String customerEmail;
    private final TicketPriority priority;
    private final TicketStatus status;
    private final String assignedTo;
    private final Instant slaDueAt;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final long slaSecondsRemaining;
    private final boolean breached;

    public TicketResponse(
        Long id,
        String title,
        String description,
        String customerName,
        String customerEmail,
        TicketPriority priority,
        TicketStatus status,
        String assignedTo,
        Instant slaDueAt,
        Instant createdAt,
        Instant updatedAt,
        long slaSecondsRemaining,
        boolean breached
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.priority = priority;
        this.status = status;
        this.assignedTo = assignedTo;
        this.slaDueAt = slaDueAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.slaSecondsRemaining = slaSecondsRemaining;
        this.breached = breached;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public Instant getSlaDueAt() {
        return slaDueAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getSlaSecondsRemaining() {
        return slaSecondsRemaining;
    }

    public boolean isBreached() {
        return breached;
    }
}
