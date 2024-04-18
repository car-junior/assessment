package com.senior.assessment.domain.service;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.repository.ItemRepository;
import com.senior.assessment.domain.repository.OrderRepository;
import com.senior.assessment.infrastructure.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(Order order) {
        addOrderInOrderItems(order);
        var items = getItemsByIds(extractItemIds(order));
        updateItemsInOrderItems(order, items);
        return orderRepository.save(order);
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> CustomException.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .message(String.format("Cannot found order with id %s.", orderId))
                        .build()
                );
    }

//    public Item updateItem(UUID itemId, Item item) {
//        assertExistsItemById(itemId);
//        assertNotExistsItemByNameAndTypeAndIdNot(item.getName(), item.getType(), itemId);
//        item.setId(itemId);
//        return itemRepository.save(item);
//    }
//
//    public Item getItemById(UUID itemId) {
//        return itemRepository.findById(itemId)
//                .orElseThrow(() -> CustomException.builder()
//                        .httpStatus(HttpStatus.NOT_FOUND)
//                        .message(String.format("Cannot found item with id %s.", itemId))
//                        .build()
//                );
//    }
//
//    // TODO: Depois adicionar validação que olha em ordem items
//    // Não deve ser possível excluir um produto/serviço se ele estiver associado a algum pedido
//    public void deleteItemById(UUID itemId) {
//        assertExistsItemById(itemId);
//        assertNotLinkedItemWithOrder(itemId);
//        itemRepository.deleteById(itemId);
//    }
//
//    public Page<Item> getAllItem(ItemSearch itemSearch, Pageable pagination) {
//        return itemRepository.findAll(ItemDslPredicate.expression(itemSearch), pagination);
//    }
//
//    // privates methods
//
//    private void assertExistsItemById(UUID itemId) {
//        if (!itemRepository.existsItemById(itemId))
//            throw CustomException.builder()
//                    .httpStatus(HttpStatus.NOT_FOUND)
//                    .message(String.format("Cannot found item with id %s.", itemId))
//                    .build();
//    }
//
//    private void assertNotExistsItemByNameAndType(String name, ItemType type) {
//        if (itemRepository.existsItemByNameAndType(name, type))
//            throw CustomException.builder()
//                    .httpStatus(HttpStatus.BAD_REQUEST)
//                    .message(String.format("Already item with this name: %s, type: %s.", name, type))
//                    .build();
//    }
//
//    private void assertNotExistsItemByNameAndTypeAndIdNot(String name, ItemType type, UUID id) {
//        if (itemRepository.existsItemByNameAndTypeAndIdNot(name, type, id))
//            throw CustomException.builder()
//                    .httpStatus(HttpStatus.BAD_REQUEST)
//                    .message(String.format("Already item with this name: %s, type: %s.", name, type))
//                    .build();
//    }
//
//    private void assertNotLinkedItemWithOrder(UUID itemId) {
//        if (orderItemRepository.existsOrderItemByItemId(itemId))
//            throw CustomException.builder()
//                    .httpStatus(HttpStatus.BAD_REQUEST)
//                    .message("Cannot delete item because have linked order.")
//                    .build();
//    }

    private void addOrderInOrderItems(Order order) {
        order.getOrderItems()
                .forEach(oi -> oi.setOrder(order));
    }

    private void updateItemsInOrderItems(Order order, Set<Item> items) {
        var itemsMap = new HashMap<UUID, Item>();
        items.forEach(item -> itemsMap.put(item.getId(), item));
        order.getOrderItems().forEach(orderItem -> orderItem.setItem(itemsMap.get(orderItem.getItem().getId())));
    }

    private Set<UUID> extractItemIds(Order order) {
        return order.getOrderItems().stream()
                .map(orderItem -> orderItem.getItem().getId())
                .collect(Collectors.toSet());
    }

    private Set<Item> getItemsByIds(Set<UUID> itemsIds) {
        var items = itemRepository.getAllByIdIn(itemsIds);
        assertExistsAllItems(itemsIds, items);
        assertAllItemsIsActive(items);
        return items;
    }

    private void assertExistsAllItems(Set<UUID> itemsIds, Set<Item> items) {
        var foundItemsIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toSet());

        var missingItemIds = itemsIds.stream()
                .filter(itemId -> !foundItemsIds.contains(itemId))
                .collect(Collectors.toSet());

        if (!missingItemIds.isEmpty())
            throw CustomException.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message(String.format("Cannot found items: [%s].", missingItemIds))
                    .build();
    }

    private void assertAllItemsIsActive(Set<Item> items) {
        var itemIdsDisabled = items.stream()
                .filter(item -> item.getStatus() == ItemStatus.DISABLED)
                .map(Item::getId)
                .collect(Collectors.toSet());

        if (!itemIdsDisabled.isEmpty())
            throw CustomException.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message(String.format(
                                    "Cannot add items: %s to order because are %s.",
                                    itemIdsDisabled,
                                    ItemStatus.DISABLED
                            )
                    ).build();
    }

}
