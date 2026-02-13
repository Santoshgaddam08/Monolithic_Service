package com.example.monolith_service.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketAssignRequest {

    @NotBlank(message = "assignedTo is required")
    @Size(max = 120, message = "assignedTo must be at most 120 characters")
    private String assignedTo;

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}
