package com.senior.assessment.domain.dto.item;

import com.senior.assessment.domain.enums.ItemStatus;
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
public class ItemDetailDetailDto {
    private UUID id;
    private String name;
    private ItemType type;
    private BigDecimal price;
    private ItemStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
