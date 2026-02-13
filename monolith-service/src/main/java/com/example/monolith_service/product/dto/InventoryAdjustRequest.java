package com.example.monolith_service.product.dto;

import jakarta.validation.constraints.NotNull;

public class InventoryAdjustRequest {

    @NotNull(message = "delta is required")
    private Integer delta;

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }
}
