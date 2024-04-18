package com.senior.assessment.domain.service;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.enums.OrderStatus;
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
        var items = getItemsByIds(extractItemIds(order));
        addOrderInOrderItems(order);
        updateItemsInOrderItems(order, items);
        assertCanApplyDiscount(order);
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

    // private methods

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

    private void assertCanApplyDiscount(Order order) {
        assertOrderIsOpen(order);
        assertContainsItemProduct(order);
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

    private void assertOrderIsOpen(Order order) {
        if (order.getStatus() == OrderStatus.CLOSED)
            throw CustomException.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message(String.format("Cannot apply discount in order %s.", OrderStatus.CLOSED))
                    .build();
    }

    private void assertContainsItemProduct(Order order) {
        var notContainsItemProduct = order.getOrderItems().stream()
                .filter(orderItem -> orderItem.getItem().getType() == ItemType.PRODUCT)
                .findAny()
                .isEmpty();

        if (notContainsItemProduct)
            throw CustomException.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message(String.format(
                            "Cannot apply discount in order because not contain item %s.",
                            ItemType.PRODUCT)
                    ).build();
    }

}
