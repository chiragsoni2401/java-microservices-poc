package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryDTO;
import com.ecommerce.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@Slf4j
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDTO> getInventory(@PathVariable String productId) {
        log.info("Fetching inventory for product: {}", productId);
        InventoryDTO inventory = inventoryService.getInventory(productId);
        return ResponseEntity.ok(inventory);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryDTO> updateInventory(
            @PathVariable String productId,
            @RequestParam Integer quantity) {
        log.info("Updating inventory for product: {} with quantity: {}", productId, quantity);
        InventoryDTO updated = inventoryService.updateInventory(productId, quantity);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<InventoryDTO> reserveInventory(
            @PathVariable String productId,
            @RequestParam Integer quantity) {
        log.info("Reserving inventory for product: {} with quantity: {}", productId, quantity);
        InventoryDTO reserved = inventoryService.reserveInventory(productId, quantity);
        return ResponseEntity.ok(reserved);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Inventory Service");
        return ResponseEntity.ok(response);
    }
}
