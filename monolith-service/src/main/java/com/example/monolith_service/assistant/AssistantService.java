package com.example.monolith_service.assistant;

import com.example.monolith_service.assistant.dto.ChatResponse;
import com.example.monolith_service.product.Product;
import com.example.monolith_service.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class AssistantService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final ProductRepository productRepository;

    public AssistantService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ChatResponse chat(String rawMessage) {
        String message = rawMessage == null ? "" : rawMessage.trim().toLowerCase();
        List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            return new ChatResponse(
                "There are no products yet. Add products first, then I can analyze stock, value, and risks.",
                defaultSuggestions()
            );
        }

        if (containsAny(message, "low stock", "restock", "reorder")) {
            List<Product> low = lowStock(products);
            if (low.isEmpty()) {
                return new ChatResponse("Great news. No low-stock products right now.", defaultSuggestions());
            }
            String details = low.stream()
                .limit(5)
                .map(p -> p.getName() + " (" + p.getSku() + "): qty " + p.getQuantity())
                .collect(Collectors.joining("; "));
            return new ChatResponse("Top low-stock items: " + details, defaultSuggestions());
        }

        if (containsAny(message, "inventory value", "stock value", "value")) {
            BigDecimal totalValue = products.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new ChatResponse(
                "Current inventory value is " + formatCurrency(totalValue) + ".",
                defaultSuggestions()
            );
        }

        if (containsAny(message, "out of stock", "zero stock")) {
            List<Product> out = products.stream().filter(p -> p.getQuantity() == 0).toList();
            if (out.isEmpty()) {
                return new ChatResponse("No products are out of stock.", defaultSuggestions());
            }
            String names = out.stream().limit(5).map(Product::getName).collect(Collectors.joining(", "));
            return new ChatResponse("Out-of-stock products: " + names, defaultSuggestions());
        }

        if (containsAny(message, "most expensive", "highest price", "expensive")) {
            Product max = products.stream().max(Comparator.comparing(Product::getPrice)).orElse(products.get(0));
            return new ChatResponse(
                "Highest priced product is " + max.getName() + " (" + max.getSku() + ") at " + formatCurrency(max.getPrice()) + ".",
                defaultSuggestions()
            );
        }

        if (containsAny(message, "summary", "dashboard", "overview", "kpi")) {
            long lowCount = lowStock(products).size();
            long outCount = products.stream().filter(p -> p.getQuantity() == 0).count();
            BigDecimal totalValue = products.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new ChatResponse(
                "Overview: products=" + products.size()
                    + ", lowStock=" + lowCount
                    + ", outOfStock=" + outCount
                    + ", inventoryValue=" + formatCurrency(totalValue) + ".",
                defaultSuggestions()
            );
        }

        return new ChatResponse(
            "I can help with inventory insights. Ask things like: low stock, inventory value, out of stock, or summary.",
            defaultSuggestions()
        );
    }

    private static List<Product> lowStock(List<Product> products) {
        return products.stream()
            .filter(p -> p.getQuantity() <= LOW_STOCK_THRESHOLD)
            .sorted(Comparator.comparing(Product::getQuantity))
            .toList();
    }

    private static boolean containsAny(String text, String... keys) {
        for (String key : keys) {
            if (text.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private static String formatCurrency(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }

    private static List<String> defaultSuggestions() {
        return List.of(
            "Give me a summary",
            "Show low stock items",
            "What is my inventory value?",
            "Which products are out of stock?"
        );
    }
}
