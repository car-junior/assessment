package com.senior.assessment.domain.dto.order;

import com.senior.assessment.domain.enums.ItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemDetailDto {
    private UUID id;
    private int amount;
    private ItemDetailDto item;
    private BigDecimal itemPrice;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ItemDetailDto {
        private UUID id;
        private String name;
        private ItemType type;
    }
}
