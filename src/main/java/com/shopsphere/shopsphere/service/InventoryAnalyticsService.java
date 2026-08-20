package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.ProductResponse;
import com.shopsphere.shopsphere.entity.Product;
import com.shopsphere.shopsphere.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InventoryAnalyticsService {

    private final ProductRepository productRepository;

    private static final int LOW_STOCK_THRESHOLD = 10;

    /**
     * Low-stock alert: returns products with stock below threshold,
     * ordered from lowest stock to highest using a min-heap.
     */
    public List<ProductResponse> getLowStockAlerts() {
        List<Product> allProducts = productRepository.findAll();

        // Min-heap ordered by stockQuantity ascending
        PriorityQueue<Product> minHeap = new PriorityQueue<>(
                Comparator.comparingInt(Product::getStockQuantity)
        );

        for (Product product : allProducts) {
            if (product.getStockQuantity() < LOW_STOCK_THRESHOLD) {
                minHeap.offer(product);
            }
        }

        List<ProductResponse> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            result.add(mapToResponse(minHeap.poll()));
        }

        return result;
    }

    /**
     * Top-selling products: returns the top K products by unitsSold,
     * using a min-heap of fixed size K (classic "top-K" pattern).
     */
    public List<ProductResponse> getTopSellingProducts(int k) {
        List<Product> allProducts = productRepository.findAll();

        // Min-heap ordered by unitsSold ascending — smallest is always at the root,
        // so we can efficiently evict the smallest when the heap grows past size k
        PriorityQueue<Product> minHeap = new PriorityQueue<>(
                Comparator.comparingInt(Product::getUnitsSold)
        );

        for (Product product : allProducts) {
            minHeap.offer(product);
            if (minHeap.size() > k) {
                minHeap.poll(); // remove the smallest, keeping only the top k
            }
        }

        // Heap now holds the top k, but in ascending order — reverse for descending display
        List<ProductResponse> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            result.add(mapToResponse(minHeap.poll()));
        }
        Collections.reverse(result);

        return result;
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .unitsSold(product.getUnitsSold())
                .categoryName(product.getCategory().getName())
                .build();
    }
}