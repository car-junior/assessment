package com.senior.assessment.domain.repository;

import com.senior.assessment.domain.config.AssessmentConfigTest;
import com.senior.assessment.domain.config.PostgreSQLContainerConfig;
import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.entity.OrderItem;
import com.senior.assessment.domain.enums.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import(AssessmentConfigTest.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderItemRepositoryTest extends PostgreSQLContainerConfig {
    private Item item;
    private Order order;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    public void setup() {
        item = Item.builder()
                .name("Ebook")
                .type(ItemType.PRODUCT)
                .price(BigDecimal.valueOf(50.00))
                .build();
        itemRepository.save(item);
        order = new Order();
        var orderItem = OrderItem.builder()
                .amount(2)
                .item(item)
                .order(order)
                .itemPrice(item.getPrice())
                .build();
        order.getOrderItems().add(orderItem);
        orderRepository.save(order);
    }

    @Test
    void testGivenItemId_whenExistsOrderItemByItemId_thenReturnTrue() {
        // Given / Arrange

        // When / Act
        var existsOrderItem = orderItemRepository.existsOrderItemByItemId(item.getId());

        // Then / Assert
        assertTrue(existsOrderItem);
    }

    @Test
    void testGivenNonExistingItemId_whenExistsOrderItemByItemId_thenReturnFalse() {
        // Given / Arrange

        // When / Act
        var existsOrderItem = orderItemRepository.existsOrderItemByItemId(UUID.randomUUID());

        // Then / Assert
        assertFalse(existsOrderItem);
    }

    @Test
    void testGivenExistingOrderItemsIds_whenGetAllByIdIn_thenReturnOrderItems() {
        // Given / Arrange
        var orderItemsIds = order.getOrderItems().stream()
                .map(OrderItem::getId)
                .collect(Collectors.toSet());

        // When / Act
        var orderItems = orderItemRepository.getAllByIdIn(orderItemsIds);

        // Then / Assert
        assertNotNull(orderItems);
        assertThat(orderItems).containsAll(order.getOrderItems());
    }

    @Test
    void testGivenNonExistingOrderItemsIds_whenGetAllByIdIn_thenReturnEmptyOrderItems() {
        // Given / Arrange
        var orderItemsIds = Set.of(UUID.randomUUID());

        // When / Act
        var orderItems = orderItemRepository.getAllByIdIn(orderItemsIds);

        // Then / Assert
        assertNotNull(orderItems);
        assertThat(orderItems).isEmpty();
    }
}
