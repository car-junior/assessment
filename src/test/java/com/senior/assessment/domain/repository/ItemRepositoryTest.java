package com.senior.assessment.domain.repository;

import com.senior.assessment.domain.config.AssessmentConfigTest;
import com.senior.assessment.domain.config.BaseIntegrationConfigurationTest;
import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.enums.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import(AssessmentConfigTest.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ItemRepositoryTest extends BaseIntegrationConfigurationTest {
    private Item productItem;
    private Item serviceItem;
    @Autowired
    private ItemRepository itemRepository;


    @BeforeEach
    public void setup() {
        productItem = Item.builder()
                .name("Mouse Pad")
                .type(ItemType.PRODUCT)
                .price(BigDecimal.valueOf(20.00))
                .build();
        serviceItem = Item.builder()
                .name("Formatar Computadores")
                .type(ItemType.SERVICE)
                .price(BigDecimal.valueOf(20.00))
                .build();
        itemRepository.saveAll(List.of(productItem, serviceItem));
    }

    @Test
    void testGivenNonExistingItemId_whenExistsItemById_thenReturnFalse() {
        // Given / Arrange

        // When / Act
        var result = itemRepository.existsItemById(UUID.randomUUID());

        // Then / Assert
        assertFalse(result);
    }

    @Test
    void testGivenItemId_whenExistsItemById_thenReturnTrue() {
        // Given / Arrange

        // When / Act
        var result = itemRepository.existsItemById(productItem.getId());

        // Then / Assert
        assertTrue(result);
    }

    @Test
    void testGivenItemNameAndType_whenExistsItemByNameAndType_thenReturnTrue() {
        // Given / Arrange

        // When / Act
        var result = itemRepository.existsItemByNameAndType(productItem.getName(), productItem.getType());

        // Then / Assert
        assertTrue(result);
    }
    @Test
    void testGivenNonExistingItemByNameAndType_whenExistsItemByNameAndType_thenReturnFalse() {
        // Given / Arrange

        // When / Act
        var result = itemRepository.existsItemByNameAndType("Ebook", ItemType.PRODUCT);

        // Then / Assert
        assertFalse(result);
    }

    @Test
    void testGivenItemNameAndTypeAndId_whenExistsItemByNameAndTypeAndIdNot_thenReturnTrue() {
        // Given / Arrange
        var newName = "Mouse Pad";
        var newType = ItemType.PRODUCT;

        // When / Act
        var result = itemRepository.existsItemByNameAndTypeAndIdNot(newName, newType, serviceItem.getId());

        // Then / Assert
        assertTrue(result);
    }

    @Test
    void testGivenNonExistingItemByNameAndTypeAndId_whenExistsItemByNameAndTypeAndIdNot_thenReturnFalse() {
        // Given / Arrange
        var newName = "Teclado";
        var newType = ItemType.PRODUCT;

        // When / Act
        var result = itemRepository.existsItemByNameAndTypeAndIdNot(newName, newType, serviceItem.getId());

        // Then / Assert
        assertFalse(result);
    }

    @Test
    void testGivenItemsIds_whenGetAllByIdIn_thenReturnSetItem() {
        // Given / Arrange
        var itemsIds = Set.of(productItem.getId(), serviceItem.getId());

        // When / Act
        var result = itemRepository.getAllByIdIn(itemsIds);

        // Then / Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(productItem, serviceItem)));
    }

    @Test
    void testGivenNonExistingItemsIds_whenGetAllByIdIn_thenReturnEmptySet() {
        // Given / Arrange
        var itemsIds = Set.of(UUID.randomUUID(), UUID.randomUUID());

        // When / Act
        var result = itemRepository.getAllByIdIn(itemsIds);

        // Then / Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
