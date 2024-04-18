package com.senior.assessment.domain.dto.order.detailslist;

import com.senior.assessment.domain.enums.ItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemDetailDetailDto {
    private UUID id;
    private int amount;
    private ItemDetailDto item;
    private BigDecimal itemPrice;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ItemDetailDto {
        private UUID id;
        private String name;
        private ItemType type;
    }
}
