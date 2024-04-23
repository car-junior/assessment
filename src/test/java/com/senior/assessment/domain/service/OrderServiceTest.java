package com.senior.assessment.domain.service;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.entity.OrderItem;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.enums.OrderStatus;
import com.senior.assessment.domain.repository.ItemRepository;
import com.senior.assessment.domain.repository.OrderItemRepository;
import com.senior.assessment.domain.repository.OrderRepository;
import com.senior.assessment.infrastructure.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.senior.assessment.domain.enums.ItemStatus.DISABLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    private UUID itemIdOne;
    private UUID itemIdTwo;
    private Order order;
    private final Set<Item> items = new HashSet<>();
    private final List<Item> itemsToOrder = new ArrayList<>();

    @BeforeEach
    public void setup() {
        itemIdOne = UUID.randomUUID();
        itemIdTwo = UUID.randomUUID();

        items.clear();
        items.addAll(createItems());

        itemsToOrder.clear();
        itemsToOrder.addAll(createItemsToOrder());

        order = Order.builder()
                .orderItems(createOrderItems())
                .build();
    }

    @Test
    void testGivenOrder_whenCreateOrder_thenReturnSavedOrder() {
        // Given / Arrange
        given(itemRepository.getAllByIdIn(any())).willReturn(items);
        given(orderRepository.save(any(Order.class))).willReturn(order);
        order.setDiscount(0.6);

        // When / Act
        var savedOrder = orderService.createOrder(order);

        // Then / Assert
        verify(orderRepository, times(1)).save(order);
        assertNotNull(savedOrder);
        assertEquals(0.6, savedOrder.getDiscount());
        assertEquals(2, savedOrder.getOrderItems().size());
        savedOrder.getOrderItems().forEach(orderItem -> {
            assertNotNull(orderItem.getItemPrice());
            assertNotNull(orderItem.getOrder());
        });
    }

    @Test
    void testGivenOrderWithNonExistingItems_whenCreateOrder_thenThrowsCustomException() {
        // Given / Arrange

        //Seta items inexistentes para o teste
        order.getOrderItems().forEach(orderItem -> orderItem.getItem().setId(UUID.randomUUID()));
        var missingItemIds = order.getOrderItems().stream()
                .map(orderItem -> orderItem.getItem().getId())
                .collect(Collectors.toSet());

        given(itemRepository.getAllByIdIn(any())).willReturn(items);

        // When / Act
        var customException = assertThrows(CustomException.class, () -> orderService.createOrder(order));

        // Then / Assert
        verify(orderRepository, never()).save(order);
        assertEquals(HttpStatus.NOT_FOUND, customException.getHttpStatus());
        assertEquals(String.format("Cannot found items: %s.", missingItemIds), customException.getMessage());
    }

    @Test
    void testGivenOrderWithProductItemDisabled_whenCreateOrder_thenThrowsCustomException() {
        // Given / Arrange

        var productsDisabled = new HashSet<UUID>();
        // Seta product item com status DISABLED para o teste
        items.stream()
                .filter(item -> item.getType() == ItemType.PRODUCT)
                .forEach(item -> {
                    item.setStatus(DISABLED);
                    productsDisabled.add(item.getId());
                });

        given(itemRepository.getAllByIdIn(any())).willReturn(items);

        // When / Act
        var customException = assertThrows(CustomException.class, () -> orderService.createOrder(order));

        // Then / Assert
        verify(orderRepository, never()).save(order);
        assertEquals(HttpStatus.BAD_REQUEST, customException.getHttpStatus());
        assertEquals(
                String.format("Cannot add products items: %s to order because are %s.", productsDisabled, DISABLED),
                customException.getMessage()
        );
    }

    @Test
    void testGivenOrderWithDiscountAndNonExistingProductItem_whenCreateOrder_thenThrowsCustomException() {
        // Given / Arrange

        // Adiciona desconto
        order.setDiscount(0.5);
        // Seta todos items que vão ser vinculados a order do tipo PRODUCT para SERVICE
        items.stream()
                .filter(item -> item.getType() == ItemType.PRODUCT)
                .forEach(item -> item.setType(ItemType.SERVICE));

        given(itemRepository.getAllByIdIn(any())).willReturn(items);

        // When / Act
        var customException = assertThrows(CustomException.class, () -> orderService.createOrder(order));

        // Then / Assert
        verify(orderRepository, never()).save(order);
        assertEquals(HttpStatus.BAD_REQUEST, customException.getHttpStatus());
        assertEquals(
                String.format("Cannot apply discount in order because not contain item %s.", ItemType.PRODUCT),
                customException.getMessage()
        );
    }

    @Test
    void testGivenOrder_whenUpdateOrder_thenReturnUpdatedOrder() {
        // Given / Arrange
        var orderId = UUID.randomUUID();

        // Order existente/db
        order.setId(orderId);
        order.getOrderItems().get(0).setId(UUID.randomUUID());
        order.getOrderItems().get(1).setId(UUID.randomUUID());

        // Muda preço dos items
        items.forEach(item -> item.setPrice(BigDecimal.valueOf(20.0)));

        // Novas informações para order existente
        var updatedOrderInfo = Order.builder()
                .discount(0.8)
                .orderItems(createOrderItems())
                .build();
        updatedOrderInfo.getOrderItems().get(0).setAmount(5);
        updatedOrderInfo.getOrderItems().get(1).setAmount(10);


        given(orderRepository.findById(any(UUID.class))).willReturn(Optional.of(order));
        given(orderItemRepository.getAllByIdIn(any())).willReturn(Collections.emptySet());
        given(itemRepository.getAllByIdIn(any())).willReturn(items);
        given(orderRepository.save(any(Order.class))).willReturn(order);

        // When / Act
        var updatedOrder = orderService.updateOrder(orderId, updatedOrderInfo);

        // Then / Assert
        verify(orderRepository, times(1)).save(order);
        assertNotNull(updatedOrder);
        assertEquals(0.8, updatedOrder.getDiscount());
        assertEquals(5, updatedOrder.getOrderItems().get(0).getAmount());
        assertThat(BigDecimal.valueOf(20.0)).isEqualByComparingTo(updatedOrder.getOrderItems().get(0).getItemPrice());
        assertEquals(10, updatedOrder.getOrderItems().get(1).getAmount());
        assertThat(BigDecimal.valueOf(20.0)).isEqualByComparingTo(updatedOrder.getOrderItems().get(1).getItemPrice());
    }

    @Test
    void testGivenNonExistingOrderId_whenUpdateOrder_thenThrowsCustomException() {
        // Given / Arrange
        var orderId = UUID.randomUUID();

        given(orderRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // When / Act
        var customException = assertThrows(
                CustomException.class, () -> orderService.updateOrder(orderId, mock(Order.class))
        );

        // Then / Assert
        verify(orderRepository, never()).save(order);
        assertEquals(HttpStatus.NOT_FOUND, customException.getHttpStatus());
        assertEquals(String.format("Cannot found order with id %s.", orderId), customException.getMessage());
    }

    @Test
    void testGivenOrderIdClosed_whenUpdateOrder_thenThrowsCustomException() {
        // Given / Arrange
        var orderId = UUID.randomUUID();
        order.setStatus(OrderStatus.CLOSED);

        given(orderRepository.findById(any(UUID.class))).willReturn(Optional.of(order));

        // When / Act
        var customException = assertThrows(
                CustomException.class, () -> orderService.updateOrder(orderId, mock(Order.class))
        );

        // Then / Assert
        verify(orderRepository, never()).save(order);
        assertEquals(HttpStatus.BAD_REQUEST, customException.getHttpStatus());
        assertEquals(String.format("Cannot edit order because is %s.", OrderStatus.CLOSED), customException.getMessage());
    }

    @Test
    void testGivenOrderWithNonExistingOrderItemId_whenUpdateOrder_thenThrowsCustomException() {
        // Given / Arrange
        var orderId = UUID.randomUUID();
        var nonExistingOrderItems = createOrderItems().stream()
                .peek(orderItem -> orderItem.setId(UUID.randomUUID()))
                .toList();
        var missingOrderItemsIds = nonExistingOrderItems.stream()
                .map(OrderItem::getId)
                .collect(Collectors.toSet());
        var updatedOrderInfo = Order.builder()
                .discount(0.8)
                .orderItems(nonExistingOrderItems)
                .build();

        given(orderRepository.findById(any(UUID.class))).willReturn(Optional.of(order));
        given(orderItemRepository.getAllByIdIn(any())).willReturn(Collections.emptySet());

        // When / Act
        var customException = assertThrows(
                CustomException.class, () -> orderService.updateOrder(orderId, updatedOrderInfo)
        );

        // Then / Assert
        verify(orderRepository, never()).save(order);
        assertEquals(HttpStatus.NOT_FOUND, customException.getHttpStatus());
        assertEquals(String.format("Cannot found order items: %s.", missingOrderItemsIds), customException.getMessage());
    }

    @Test
    void testGivenOrderWithNonExistingItems_whenUpdateOrder_thenThrowsCustomException() {
        // Given / Arrange

        var updatedOrderInfo = Order.builder()
                .discount(0.8)
                .orderItems(createOrderItems())
                .build();
        //Seta items inexistentes para o teste
        updatedOrderInfo.getOrderItems().forEach(orderItem -> orderItem.getItem().setId(UUID.randomUUID()));
        var missingItemIds = order.getOrderItems().stream()
                .map(orderItem -> orderItem.getItem().getId())
                .collect(Collectors.toSet());

        var orderId = UUID.randomUUID();

        given(orderRepository.findById(any(UUID.class))).willReturn(Optional.of(order));
        given(orderItemRepository.getAllByIdIn(any())).willReturn(Collections.emptySet());
        given(itemRepository.getAllByIdIn(any())).willReturn(items);

        // When / Act
        var customException = assertThrows(
                CustomException.class, () -> orderService.updateOrder(orderId, updatedOrderInfo));

        // Then / Assert
        verify(orderRepository, never()).save(order);
        assertEquals(HttpStatus.NOT_FOUND, customException.getHttpStatus());
        assertEquals(String.format("Cannot found items: %s.", missingItemIds), customException.getMessage());
    }

    @Test
    void testGivenOrderWithDiscountAndNonExistingProductItem_whenUpdateOrder_thenThrowsCustomException() {
        // Given / Arrange

        // Adiciona desconto
        var updatedOrderInfo = Order.builder()
                .discount(0.8)
                .orderItems(createOrderItems())
                .build();
        // Seta todos items que vão ser vinculados a order do tipo PRODUCT para SERVICE
        items.stream()
                .filter(item -> item.getType() == ItemType.PRODUCT)
                .forEach(item -> item.setType(ItemType.SERVICE));

        var orderId = UUID.randomUUID();

        given(orderRepository.findById(any(UUID.class))).willReturn(Optional.of(order));
        given(orderItemRepository.getAllByIdIn(any())).willReturn(Collections.emptySet());
        given(itemRepository.getAllByIdIn(any())).willReturn(items);

        // When / Act
        var customException = assertThrows(
                CustomException.class, () -> orderService.updateOrder(orderId, updatedOrderInfo)
        );

        // Then / Assert
        verify(orderRepository, never()).save(order);
        assertEquals(HttpStatus.BAD_REQUEST, customException.getHttpStatus());
        assertEquals(
                String.format("Cannot apply discount in order because not contain item %s.", ItemType.PRODUCT),
                customException.getMessage()
        );
    }

    @Test
    void testGivenOrderId_whenDeleteOrderById_thenReturnNothing() {
        // Given / Arrange
        var orderId = UUID.randomUUID();
        order.setId(orderId);

        given(orderRepository.findById(any(UUID.class))).willReturn(Optional.of(order));

        // When / Act
        orderService.deleteOrderById(orderId);

        // Then / Assert
        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    void testGivenNonExistingOrderId_whenDeleteOrderById_thenThrowsCustomException() {
        // Given / Arrange
        var orderId = UUID.randomUUID();

        given(orderRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // When / Act
        var customException = assertThrows(
                CustomException.class, () -> orderService.deleteOrderById(orderId));

        // Then / Assert
        verify(itemRepository, never()).deleteById(orderId);
        assertEquals(HttpStatus.NOT_FOUND, customException.getHttpStatus());
        assertEquals(
                String.format("Cannot found order with id %s.", orderId),
                customException.getMessage()
        );
    }

    @Test
    void testGivenOrderIdClosed_whenDeleteOrderById_thenThrowsCustomException() {
        // Given / Arrange
        var orderId = UUID.randomUUID();
        order.setId(orderId);
        order.setStatus(OrderStatus.CLOSED);

        given(orderRepository.findById(any(UUID.class))).willReturn(Optional.of(order));

        // When / Act
        var customException = assertThrows(
                CustomException.class, () -> orderService.deleteOrderById(orderId));

        // Then / Assert
        verify(orderRepository, never()).deleteById(orderId);
        assertEquals(HttpStatus.BAD_REQUEST, customException.getHttpStatus());
        assertEquals(
                String.format("Cannot delete order %s.", OrderStatus.CLOSED),
                customException.getMessage()
        );
    }

    @Test
    void testGivenOrderId_whenGetOrderById_thenReturnOrder() {
        // Given / Arrange
        var orderId = UUID.randomUUID();
        order.setId(orderId);

        given(orderRepository.findById(any(UUID.class))).willReturn(Optional.of(order));

        // When / Act
        var foundOrder = orderService.getOrderById(orderId);

        // Then / Assert
        assertNotNull(foundOrder);
        assertEquals(orderId, foundOrder.getId());
        assertThat(foundOrder.getOrderItems()).isNotEmpty();
        assertEquals(2, foundOrder.getOrderItems().size());
        assertThat(foundOrder.getDiscount()).isEqualTo(order.getDiscount());
    }

    @Test
    void testGivenNonExistsOrderId_whenGetOrderById_thenThrowsCustomException() {
        // Given / Arrange
        var orderId = UUID.randomUUID();

        given(orderRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // When / Act
        var customException = assertThrows(
                CustomException.class, () -> orderService.getOrderById(orderId)
        );

        // Then / Assert
        assertEquals(HttpStatus.NOT_FOUND, customException.getHttpStatus());
        assertEquals(
                String.format("Cannot found order with id %s.", orderId),
                customException.getMessage()
        );
    }

    private Set<Item> createItems() {
        var itemOne = Item.builder()
                .id(itemIdOne)
                .name("Ebook")
                .type(ItemType.PRODUCT)
                .price(BigDecimal.valueOf(10.00))
                .build();
        var itemTwo = Item.builder()
                .id(itemIdTwo)
                .name("Formatar Computadores")
                .type(ItemType.SERVICE)
                .price(BigDecimal.valueOf(20.00))
                .build();
        return Set.of(itemOne, itemTwo);
    }

    private Set<Item> createItemsToOrder() {
        var itemOne = Item.builder()
                .id(itemIdOne)
                .build();
        var itemTwo = Item.builder()
                .id(itemIdTwo)
                .build();
        return Set.of(itemOne, itemTwo);
    }

    private List<OrderItem> createOrderItems() {
        var orderItems = new ArrayList<OrderItem>();
        orderItems.add(OrderItem.builder()
                .amount(2)
                .item(itemsToOrder.get(0))
                .build());
        orderItems.add(OrderItem.builder()
                .amount(2)
                .item(itemsToOrder.get(1))
                .build());
        return orderItems;
    }
}
