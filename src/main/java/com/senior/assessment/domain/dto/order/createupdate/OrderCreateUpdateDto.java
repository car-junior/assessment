package com.senior.assessment.domain.dto.order.createupdate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateUpdateDto {

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Digits(integer = 1, fraction = 2)
    private double discount;

    @Valid
    @NotNull
    @Size(min = 1)
    private Set<OrderItemDto> orderItems;
}
