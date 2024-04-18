package com.senior.assessment.domain.service;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.enums.OrderStatus;
import com.senior.assessment.domain.querydsl.OrderDslPredicate;
import com.senior.assessment.domain.querydsl.search.OrderSearch;
import com.senior.assessment.domain.repository.ItemRepository;
import com.senior.assessment.domain.repository.OrderRepository;
import com.senior.assessment.infrastructure.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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
        return validationAndSave(order);
    }

    @Transactional
    public Order updateOrder(UUID orderId, Order order) {
        assertExistsOrderById(orderId);
        order.setId(orderId);
        return validationAndSave(order);
    }

    @Transactional
    public void deleteOrderById(UUID orderId) {
        var order = getOrderById(orderId);
        assertOrderIsOpen(order, String.format("Cannot delete order %s.", OrderStatus.CLOSED));
        orderRepository.deleteById(order.getId());
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

    private Order validationAndSave(Order order) {
        var items = getItemsByIds(extractItemIds(order));
        updateItemsInOrderItems(order, items);
        setOrderAndPriceForOrderItems(order);
        assertCanApplyDiscount(order);
        return orderRepository.save(order);
    }

    private void setOrderAndPriceForOrderItems(Order order) {
        order.getOrderItems()
                .forEach(orderItem -> {
                    orderItem.setOrder(order);
                    orderItem.setItemPrice(orderItem.getItem().getPrice());
                });
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
        assertOrderIsOpen(order, String.format("Cannot apply discount in order %s.", OrderStatus.CLOSED));
        hasDiscountAndContainsProductItem(order);
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

    private void assertOrderIsOpen(Order order, String message) {
        if (order.getStatus() == OrderStatus.CLOSED)
            throw CustomException.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message(message)
                    .build();
    }

    private void hasDiscountAndContainsProductItem(Order order) {
        if (order.getDiscount() > 0D) {
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

    private void assertExistsOrderById(UUID orderId) {
        if (!orderRepository.existsOrderById(orderId))
            throw CustomException.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message(String.format("Cannot found order with id %s.", orderId))
                    .build();
    }

    public Page<Order> getAllOrder(OrderSearch orderSearch, Pageable pagination) {
        return orderRepository.findAll(OrderDslPredicate.expression(orderSearch), pagination);
    }
}
