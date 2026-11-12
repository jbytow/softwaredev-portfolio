package com.portfolio.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReorderRequest {
    @NotEmpty(message = "Items are required")
    private List<@NotNull(message = "Item must not be null") @Valid OrderItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItem {
        @NotNull(message = "Id is required")
    private UUID id;
        @NotNull(message = "Display order is required")
    private Integer displayOrder;
    }
}
