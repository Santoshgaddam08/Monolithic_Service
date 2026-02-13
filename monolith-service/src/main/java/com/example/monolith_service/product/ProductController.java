package com.example.monolith_service.product;

import com.example.monolith_service.product.dto.InventoryAdjustRequest;
import com.example.monolith_service.product.dto.InventoryAdjustResponse;
import com.example.monolith_service.product.dto.ProductPageResponse;
import com.example.monolith_service.product.dto.ProductRequest;
import com.example.monolith_service.product.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @GetMapping
    public ProductPageResponse getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String direction,
        @RequestParam(required = false) String search
    ) {
        Set<String> allowedSortFields = Set.of("id", "name", "sku", "price", "quantity");
        String safeSortBy = allowedSortFields.contains(sortBy) ? sortBy : "id";
        String safeDirection = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";
        return productService.getAll(page, size, safeSortBy, safeDirection, search);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PatchMapping("/{id}/inventory")
    public InventoryAdjustResponse adjustInventory(@PathVariable Long id, @Valid @RequestBody InventoryAdjustRequest request) {
        return productService.adjustInventory(id, request.getDelta());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
