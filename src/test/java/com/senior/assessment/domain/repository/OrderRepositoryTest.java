package com.senior.assessment.domain.repository;

import com.senior.assessment.domain.config.AssessmentConfigTest;
import com.senior.assessment.domain.config.PostgreSQLContainerConfig;
import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.entity.OrderItem;
import com.senior.assessment.domain.entity.QOrder;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import(AssessmentConfigTest.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest extends PostgreSQLContainerConfig {
    private Order order;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    public void setup() {
        var item = Item.builder()
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
    void testGivenOrderIdAndNewStatus_whenUpdateStatus_thenReturnTrue() {
        // Given / Arrange
        var newStatus = OrderStatus.CLOSED;
        var qOrder = QOrder.order;
        var expressionOrderByIdAndStatus = qOrder.id.eq(order.getId())
                        .and(qOrder.status.eq(newStatus));

        // When / Act
        orderRepository.updateStatus(order.getId(), newStatus);
        var existsOrder = orderRepository.exists(expressionOrderByIdAndStatus);

        // Then / Assert
        assertTrue(existsOrder);
    }

    @Test
    void testGivenNonExistingOrderIdAndNewStatus_whenUpdateStatus_thenReturnFalse() {
        // Given / Arrange
        var newStatus = OrderStatus.CLOSED;
        var qOrder = QOrder.order;
        var expressionOrderByIdAndStatus = qOrder.id.eq(order.getId())
                .and(qOrder.status.eq(newStatus));

        // When / Act
        orderRepository.updateStatus(UUID.randomUUID(), newStatus);
        var existsOrder = orderRepository.exists(expressionOrderByIdAndStatus);

        // Then / Assert
        assertFalse(existsOrder);
    }
}
