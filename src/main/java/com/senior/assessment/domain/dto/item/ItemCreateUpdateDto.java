package com.senior.assessment.domain.dto.item;

import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateUpdateDto {
    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    private ItemType type;

    @NotNull
    @DecimalMin(value = "0.01", message = "price min is R$ 00,01.")
    private BigDecimal price;

    @NotNull
    private ItemStatus status;

    public String getName() {
        return name.trim();
    }
}
