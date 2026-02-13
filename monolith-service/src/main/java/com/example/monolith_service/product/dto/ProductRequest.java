package com.example.monolith_service.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductRequest {

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must be at most 120 characters")
    private String name;

    @NotBlank(message = "sku is required")
    @Size(max = 80, message = "sku must be at most 80 characters")
    private String sku;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "price must be zero or greater")
    @Digits(integer = 10, fraction = 2, message = "price must have up to 10 digits and 2 decimals")
    private BigDecimal price;

    @NotNull(message = "quantity is required")
    @Min(value = 0, message = "quantity must be zero or greater")
    private Integer quantity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
