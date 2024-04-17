package com.senior.assessment.domain.dto.item;

import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateDto {
    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    private ItemType type;

    @NotNull
    @Positive
    private BigDecimal price;

    private ItemStatus status;

    public String getName() {
        return name.trim();
    }
}
