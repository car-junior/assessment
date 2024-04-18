package com.senior.assessment.domain.dto.order.createupdate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemDto {
    @NotNull
    private ItemDto item;

    @NotNull
    @Positive
    private Integer amount;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ItemDto {
        @NotNull
        private UUID id;
    }
}
