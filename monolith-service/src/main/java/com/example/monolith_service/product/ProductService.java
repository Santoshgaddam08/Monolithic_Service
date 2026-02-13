package com.example.monolith_service.product;

import com.example.monolith_service.error.ResourceNotFoundException;
import com.example.monolith_service.product.dto.InventoryAdjustResponse;
import com.example.monolith_service.product.dto.ProductPageResponse;
import com.example.monolith_service.product.dto.ProductRequest;
import com.example.monolith_service.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    public ProductPageResponse getAll(int page, int size, String sortBy, String direction, String search) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort sort = "desc".equalsIgnoreCase(direction)
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        String normalizedSearch = search == null ? "" : search.trim();

        Page<Product> productPage;
        if (normalizedSearch.isEmpty()) {
            productPage = productRepository.findAll(pageable);
        } else {
            productPage = productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
                normalizedSearch,
                normalizedSearch,
                pageable
            );
        }

        return new ProductPageResponse(
            productPage.getContent().stream().map(this::toResponse).toList(),
            productPage.getNumber(),
            productPage.getSize(),
            productPage.getTotalElements(),
            productPage.getTotalPages(),
            productPage.hasNext(),
            productPage.hasPrevious()
        );
    }

    public ProductResponse getById(Long id) {
        Product product = findOrThrow(id);
        return toResponse(product);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOrThrow(id);
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        Product product = findOrThrow(id);
        productRepository.delete(product);
    }

    public InventoryAdjustResponse adjustInventory(Long id, int delta) {
        Product product = findOrThrow(id);
        int updatedQuantity = product.getQuantity() + delta;
        if (updatedQuantity < 0) {
            throw new IllegalArgumentException("Inventory cannot be negative");
        }

        product.setQuantity(updatedQuantity);
        Product saved = productRepository.save(product);
        return new InventoryAdjustResponse(saved.getId(), saved.getSku(), saved.getQuantity());
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.getName().trim());
        product.setSku(request.getSku().trim().toUpperCase());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getSku(),
            product.getPrice(),
            product.getQuantity()
        );
    }
}
