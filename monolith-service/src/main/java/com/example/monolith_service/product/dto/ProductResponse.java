package com.example.monolith_service.product.dto;

import java.math.BigDecimal;

public class ProductResponse {

    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer quantity;

    public ProductResponse(Long id, String name, String sku, BigDecimal price, Integer quantity) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
