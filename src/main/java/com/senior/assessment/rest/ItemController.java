package com.senior.assessment.rest;

import com.senior.assessment.config.mapper.ModelMapperService;
import com.senior.assessment.domain.dto.item.ItemCreateUpdateDto;
import com.senior.assessment.domain.dto.item.ItemDetailDto;
import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("items")
public class ItemController {
    private final ItemService itemService;
    private final ModelMapperService modelMapperService;

    @PostMapping
    public ResponseEntity<ItemDetailDto> create(@Valid @RequestBody ItemCreateUpdateDto itemCreate) {
        var item = itemService.createItem(modelMapperService.toObject(Item.class, itemCreate));
        return ResponseEntity.ok(modelMapperService.toObject(ItemDetailDto.class, item));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ItemDetailDto> update(@PathVariable(name = "itemId") UUID itemId,
                                                @Valid @RequestBody ItemCreateUpdateDto itemUpdate) {
        var item = modelMapperService.toObject(Item.class, itemUpdate);
        return ResponseEntity.ok(
                modelMapperService.toObject(ItemDetailDto.class, itemService.updateItem(itemId, item))
        );
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetailDto> getById(@PathVariable(name = "itemId") UUID itemId) {
        return ResponseEntity.ok(
                modelMapperService.toObject(ItemDetailDto.class, itemService.getItemById(itemId))
        );
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ItemDetailDto> deleteById(@PathVariable(name = "itemId") UUID itemId) {
        itemService.deleteItemById(itemId);
        return ResponseEntity.noContent().build();
    }

}
