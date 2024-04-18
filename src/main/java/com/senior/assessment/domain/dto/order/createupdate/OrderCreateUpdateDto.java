package com.senior.assessment.domain.dto.order.createupdate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateUpdateDto {
    @DecimalMin(value = "0.0", message = "Min discount 0.0")
    @DecimalMax(value = "1.0", message = "Max discount 1.0")
    private double discount;

    @Valid
    @NotNull
    @Size(min = 1, message = "Required min one items.")
    private List<OrderItemDto> orderItems;
}
