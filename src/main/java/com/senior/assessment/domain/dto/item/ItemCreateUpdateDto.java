package com.senior.assessment.domain.dto.item;

import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateUpdateDto {
    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    private ItemType type;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @NotNull
    private ItemStatus status;

    public String getName() {
        return (name != null) ? name.trim() : null;
    }

}
