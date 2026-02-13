package com.example.monolith_service.ticket.dto;

import com.example.monolith_service.ticket.TicketPriority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TicketRequest {

    @NotBlank(message = "title is required")
    @Size(max = 160, message = "title must be at most 160 characters")
    private String title;

    @NotBlank(message = "description is required")
    @Size(max = 1000, message = "description must be at most 1000 characters")
    private String description;

    @NotBlank(message = "customerName is required")
    @Size(max = 120, message = "customerName must be at most 120 characters")
    private String customerName;

    @NotBlank(message = "customerEmail is required")
    @Email(message = "customerEmail must be valid")
    @Size(max = 150, message = "customerEmail must be at most 150 characters")
    private String customerEmail;

    @NotNull(message = "priority is required")
    private TicketPriority priority;

    @Size(max = 120, message = "assignedTo must be at most 120 characters")
    private String assignedTo;

    @NotNull(message = "slaMinutes is required")
    @Min(value = 5, message = "slaMinutes must be at least 5")
    @Max(value = 10080, message = "slaMinutes must be at most 10080")
    private Integer slaMinutes;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Integer getSlaMinutes() {
        return slaMinutes;
    }

    public void setSlaMinutes(Integer slaMinutes) {
        this.slaMinutes = slaMinutes;
    }
}
