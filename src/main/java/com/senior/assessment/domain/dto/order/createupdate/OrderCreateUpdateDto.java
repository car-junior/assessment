package com.senior.assessment.domain.dto.order.createupdate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreateUpdateDto {

    @Digits(integer = 1, fraction = 2)
    @DecimalMin(value = "0.0", message = "Min discount 0.0")
    @DecimalMax(value = "1.0", message = "Max discount 1.0")
    private double discount;

    @Valid
    @NotNull
    @Size(min = 1, message = "Required min one items.")
    private Set<OrderItemDto> orderItems;
}
