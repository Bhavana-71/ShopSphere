package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.ProductResponse;
import com.shopsphere.shopsphere.service.InventoryAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class InventoryAnalyticsController {

    private final InventoryAnalyticsService analyticsService;

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockAlerts() {
        return ResponseEntity.ok(analyticsService.getLowStockAlerts());
    }

    @GetMapping("/top-selling")
    public ResponseEntity<List<ProductResponse>> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int k) {
        return ResponseEntity.ok(analyticsService.getTopSellingProducts(k));
    }
}