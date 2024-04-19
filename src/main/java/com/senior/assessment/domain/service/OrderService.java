package com.senior.assessment.domain.service;

import com.senior.assessment.domain.dto.order.OrderStatusChangeDto;
import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.entity.OrderItem;
import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.enums.OrderStatus;
import com.senior.assessment.domain.querydsl.OrderDslPredicate;
import com.senior.assessment.domain.querydsl.search.OrderSearch;
import com.senior.assessment.domain.repository.ItemRepository;
import com.senior.assessment.domain.repository.OrderItemRepository;
import com.senior.assessment.domain.repository.OrderRepository;
import com.senior.assessment.infrastructure.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    @Transactional
    public Order createOrder(Order order) {
        prepareOrder(order);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(UUID orderId, Order updatedOrder) {
        var order = getOrderById(orderId);
        assertOrderIsOpen(order, String.format("Cannot edit order because is %s.", OrderStatus.CLOSED));
        prepareUpdateOrder(order.getId(), updatedOrder);
        updateValues(order, updatedOrder);
        return orderRepository.save(order);
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

    public Page<Order> getAllOrder(OrderSearch orderSearch, Pageable pagination) {
        return orderRepository.findAll(OrderDslPredicate.expression(orderSearch), pagination);
    }

    @Transactional
    public void updateStatus(UUID orderId, OrderStatusChangeDto orderStatus) {
        var order = getOrderById(orderId);
        assertOrderIsOpen(order, String.format("Order %s already %s.", order.getId(), OrderStatus.CLOSED));
        orderRepository.updateStatus(order.getId(), orderStatus.getStatus());
    }

    // private methods
    private void updateValues(Order order, Order updatedOrder) {
        order.setDiscount(updatedOrder.getDiscount());
        order.getOrderItems().clear();
        order.getOrderItems().addAll(updatedOrder.getOrderItems());
        setOrderToOrderItems(order);
    }

    private void prepareOrder(Order order) {
        updateItemsToOrderItems(order.getOrderItems());
        setOrderToOrderItems(order);
        setItemPriceToNewOrderItems(order.getOrderItems());
        setOrderItemsIdNullWhenOrderIdIsNull(order);
        assertOrderMayHaveDiscount(order);
    }

    private void prepareUpdateOrder(UUID orderId, Order updatedOrder) {
        updatedOrder.setId(orderId);
        updateOrderItemsToOrder(updatedOrder);
        updateItemsToOrderItems(updatedOrder.getOrderItems());
        setItemPriceToNewOrderItems(updatedOrder.getOrderItems());
        assertOrderMayHaveDiscount(updatedOrder);
    }

    private void setOrderToOrderItems(Order order) {
        order.getOrderItems().forEach(orderItem -> orderItem.setOrder(order));
    }

    private void setItemPriceToNewOrderItems(List<OrderItem> orderItems) {
        orderItems.stream()
                .filter(orderItem -> orderItem.getId() == null)
                .forEach(orderItem -> orderItem.setItemPrice(orderItem.getItem().getPrice()));
    }

    private void setOrderItemsIdNullWhenOrderIdIsNull(Order order) {
        if (order.getId() == null) {
            order.getOrderItems()
                    .forEach(orderItem -> orderItem.setId(null));
        }
    }

    private void updateItemsToOrderItems(List<OrderItem> orderItems) {
        var items = getItems(extractItemIds(orderItems));
        var itemsMap = new HashMap<UUID, Item>();
        items.forEach(item -> itemsMap.put(item.getId(), item));
        orderItems.forEach(orderItem -> orderItem.setItem(itemsMap.get(orderItem.getItem().getId())));
    }

    private void updateOrderItemsToOrder(Order order) {
        // Lista com apenas os novos order items
        var newOrderItems = order.getOrderItems().stream()
                .filter(orderItem -> orderItem.getId() == null)
                .toList();

        // Lista dos order items vindos do banco e atualizados com as informações para atualização
        var orderItems = getOrderItems(order);
        var orderItemsMap = new HashMap<UUID, OrderItem>();
        orderItems.forEach(orderItem -> orderItemsMap.put(orderItem.getId(), orderItem));
        var updatedOrderItems = order.getOrderItems().stream()
                .filter(orderItem -> orderItemsMap.containsKey(orderItem.getId()))
                .map(updateOrderItem -> {
                    var orderItem = orderItemsMap.get(updateOrderItem.getId());
                    orderItem.setAmount(updateOrderItem.getAmount());
                    orderItem.setItem(updateOrderItem.getItem());
                    return orderItem;
                }).toList();
        order.getOrderItems().clear();
        order.getOrderItems().addAll(newOrderItems);
        order.getOrderItems().addAll(updatedOrderItems);
    }

    private Set<UUID> extractItemIds(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItem -> orderItem.getItem().getId())
                .collect(Collectors.toSet());
    }

    private Set<Item> getItems(Set<UUID> itemsIds) {
        var items = itemRepository.getAllByIdIn(itemsIds);
        assertExistsAllItems(itemsIds, items);
        assertAllProductsItemsAreActive(items);
        return items;
    }

    private Set<OrderItem> getOrderItems(Order order) {
        var orderItemsIds = order.getOrderItems().stream()
                .map(OrderItem::getId)
                .collect(Collectors.toSet());
        var orderItems = orderItemRepository.getAllByIdIn(orderItemsIds);
        assertExistsAllOrderItems(orderItemsIds, orderItems);
        return orderItems;
    }

    private void assertOrderMayHaveDiscount(Order order) {
        if (order.getDiscount() > 0D) {
            assertOrderNotHaveTypeItemsNull(order);
            assertOrderHaveProductItem(order);
        }
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

    private void assertAllProductsItemsAreActive(Set<Item> items) {
        var productsItemsDisabled = items.stream()
                .filter(item -> item.getType() == ItemType.PRODUCT && item.getStatus() == ItemStatus.DISABLED)
                .map(Item::getId)
                .collect(Collectors.toSet());

        if (!productsItemsDisabled.isEmpty())
            throw CustomException.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message(String.format(
                                    "Cannot add products items: %s to order because are %s.",
                                    productsItemsDisabled,
                                    ItemStatus.DISABLED
                            )
                    ).build();
    }

    private void assertOrderIsOpen(Order order, String message) {
        if (order.getStatus() == OrderStatus.CLOSED)
            throw CustomException.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .message(message)
                    .build();
    }

    private void assertOrderHaveProductItem(Order order) {
        var notContainsProductItem = order.getOrderItems().stream()
                .filter(orderItem -> orderItem.getItem().getType() == ItemType.PRODUCT)
                .findAny()
                .isEmpty();

        if (notContainsProductItem)
            throw CustomException.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message(String.format(
                            "Cannot apply discount in order because not contain item %s.",
                            ItemType.PRODUCT)
                    ).build();
    }

    private void assertOrderNotHaveTypeItemsNull(Order order) {
        var haveTypeItemNull = order.getOrderItems().stream()
                .anyMatch(orderItem -> orderItem.getItem().getType() == null);

        if (haveTypeItemNull)
            throw CustomException.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message("Contains items with null types, it is mandatory that all items have a type.")
                    .build();
    }

    private void assertExistsAllOrderItems(Set<UUID> orderItemsIds, Set<OrderItem> orderItems) {
        var foundOrderItemsIds = orderItems.stream()
                .map(OrderItem::getId)
                .collect(Collectors.toSet());

        var missingOrderItemsIds = orderItemsIds.stream()
                .filter(itemId -> !foundOrderItemsIds.contains(itemId))
                .collect(Collectors.toSet());

        if (!missingOrderItemsIds.isEmpty())
            throw CustomException.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message(String.format("Cannot found order items: [%s].", missingOrderItemsIds))
                    .build();
    }
}
