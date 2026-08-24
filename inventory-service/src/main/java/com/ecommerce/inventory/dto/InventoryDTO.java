package com.ecommerce.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDTO {
    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("reserved")
    private Integer reserved;

    @JsonProperty("available")
    private Integer available;
}
