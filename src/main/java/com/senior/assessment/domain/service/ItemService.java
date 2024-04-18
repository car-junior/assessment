package com.senior.assessment.domain.service;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.querydsl.ItemDslPredicate;
import com.senior.assessment.domain.querydsl.search.ItemSearch;
import com.senior.assessment.domain.repository.ItemRepository;
import com.senior.assessment.domain.repository.OrderItemRepository;
import com.senior.assessment.infrastructure.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;

    public Item createItem(Item item) {
        assertNotExistsItemByNameAndType(item.getName(), item.getType());
        return itemRepository.save(item);
    }

    public Item updateItem(UUID itemId, Item item) {
        assertExistsItemById(itemId);
        assertNotExistsItemByNameAndTypeAndIdNot(item.getName(), item.getType(), itemId);
        item.setId(itemId);
        return itemRepository.save(item);
    }

    public Item getItemById(UUID itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> CustomException.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .message(String.format("Cannot found item with id %s.", itemId))
                        .build()
                );
    }

    // TODO: Depois adicionar validação que olha em ordem items
    // Não deve ser possível excluir um produto/serviço se ele estiver associado a algum pedido
    public void deleteItemById(UUID itemId) {
        assertExistsItemById(itemId);
        assertNotLinkedItemWithOrder(itemId);
        itemRepository.deleteById(itemId);
    }

    public Page<Item> getAllItem(ItemSearch itemSearch, Pageable pagination) {
        return itemRepository.findAll(ItemDslPredicate.expression(itemSearch), pagination);
    }

    // privates methods

    private void assertExistsItemById(UUID itemId) {
        if (!itemRepository.existsItemById(itemId))
            throw CustomException.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message(String.format("Cannot found item with id %s.", itemId))
                    .build();
    }

    private void assertNotExistsItemByNameAndType(String name, ItemType type) {
        if (itemRepository.existsItemByNameAndType(name, type))
            throw CustomException.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .message(String.format("Already item with this name: %s, itemType: %s.", name, type))
                    .build();
    }

    private void assertNotExistsItemByNameAndTypeAndIdNot(String name, ItemType type, UUID id) {
        if (itemRepository.existsItemByNameAndTypeAndIdNot(name, type, id))
            throw CustomException.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .message(String.format("Already item with this name: %s, itemType: %s.", name, type))
                    .build();
    }

    private void assertNotLinkedItemWithOrder(UUID itemId) {
        if (orderItemRepository.existsOrderItemByItemId(itemId))
            throw CustomException.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .message("Cannot delete item because have linked order.")
                    .build();
    }

}
