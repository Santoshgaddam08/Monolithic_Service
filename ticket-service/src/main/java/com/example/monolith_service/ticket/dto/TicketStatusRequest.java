package com.example.monolith_service.ticket.dto;

import com.example.monolith_service.ticket.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class TicketStatusRequest {

    @NotNull(message = "status is required")
    private TicketStatus status;

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
