package com.senior.assessment.domain.service;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.entity.OrderItem;
import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
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

        // When / Act
        var savedOrder = orderService.createOrder(order);

        // Then / Assert
        verify(orderRepository, times(1)).save(order);
        assertNotNull(savedOrder);
        savedOrder.getOrderItems()
                .forEach(orderItem -> assertNotNull(orderItem.getItemPrice()));
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
        var orderItemOne = OrderItem.builder()
                .amount(2)
                .item(itemsToOrder.get(0))
                .build();
        var orderItemTwo = OrderItem.builder()
                .amount(2)
                .item(itemsToOrder.get(1))
                .build();
        return List.of(orderItemOne, orderItemTwo);
    }
}
