package com.example.monolith_service.product.dto;

public class InventoryAdjustResponse {

    private final Long productId;
    private final String sku;
    private final Integer quantity;

    public InventoryAdjustResponse(Long productId, String sku, Integer quantity) {
        this.productId = productId;
        this.sku = sku;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
