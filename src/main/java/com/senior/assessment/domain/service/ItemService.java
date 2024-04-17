package com.senior.assessment.domain.service;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.repository.ItemRepository;
import com.senior.assessment.infrastructure.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Item createItem(Item item) {
        assertNotExistsItemByNameAndType(item.getName(), item.getType());
        return itemRepository.save(item);
    }

    public Item updateItem(UUID itemId, Item item) {
        assertExistsItemById(itemId);
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
        itemRepository.deleteById(itemId);
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
                    .message(String.format("Already item with this name: %s", name))
                    .build();
    }
}
