package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.InventoryDTO;
import com.ecommerce.inventory.entity.InventoryEntity;
import com.ecommerce.inventory.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Cacheable(value = "inventory", key = "#productId")
    public InventoryDTO getInventory(String productId) {
        log.info("Fetching inventory for product: {}", productId);
        InventoryEntity inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        return convertToDTO(inventory);
    }

    @CacheEvict(value = "inventory", key = "#productId")
    public InventoryDTO updateInventory(String productId, Integer quantity) {
        log.info("Updating inventory for product: {} with quantity: {}", productId, quantity);
        
        InventoryEntity inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        inventory.setQuantity(quantity);
        InventoryEntity updated = inventoryRepository.save(inventory);
        
        return convertToDTO(updated);
    }

    @CacheEvict(value = "inventory", key = "#productId")
    public InventoryDTO reserveInventory(String productId, Integer quantity) {
        log.info("Reserving inventory for product: {} with quantity: {}", productId, quantity);
        
        InventoryEntity inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        if (inventory.getAvailable() < quantity) {
            throw new RuntimeException("Insufficient inventory for product: " + productId);
        }
        
        inventory.setReserved(inventory.getReserved() + quantity);
        InventoryEntity updated = inventoryRepository.save(inventory);
        
        return convertToDTO(updated);
    }

    private InventoryDTO convertToDTO(InventoryEntity inventory) {
        return InventoryDTO.builder()
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .reserved(inventory.getReserved())
                .available(inventory.getAvailable())
                .build();
    }
}
