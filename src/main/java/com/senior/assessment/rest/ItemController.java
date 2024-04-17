package com.senior.assessment.rest;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.repository.ItemRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("items")
public class ItemController {
    private final ItemRepository itemRepository;

    @PostMapping
    public Item create() {
        var item = Item.builder()
                .name("Teclado")
                .price(BigDecimal.valueOf(50.00))
                .type(ItemType.PRODUCT)
                .build();
        return itemRepository.save(item);
    }
}
