package com.senior.assessment.domain.service;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.repository.ItemRepository;
import com.senior.assessment.domain.repository.OrderItemRepository;
import com.senior.assessment.infrastructure.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Units tests to ItemService")
class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item originalItem;

    @BeforeEach
    public void setup() {
        originalItem = Item.builder()
                .name("Ebook")
                .type(ItemType.PRODUCT)
                .price(BigDecimal.valueOf(10.00))
                .build();
    }

    @Test
    void testGivenItem_whenCreateItem_thenReturnSavedItem() {
        // Given / Arrange
        given(itemRepository.existsItemByNameAndType(anyString(), any(ItemType.class)))
                .willReturn(false);

        given(itemRepository.save(originalItem)).willReturn(originalItem);

        // When / Act
        var savedItem = itemService.createItem(originalItem);

        // Then / Assert
        verify(itemRepository, times(1)).save(originalItem);
        assertNotNull(savedItem);
        assertEquals("Ebook", savedItem.getName());
        assertEquals(ItemType.PRODUCT, savedItem.getType());
        assertEquals(ItemStatus.ACTIVE, savedItem.getStatus());
        assertEquals(BigDecimal.valueOf(10.00), savedItem.getPrice());
    }

    @Test
    void testGivenExistingItemName_whenCreateItem_thenThrowsCustomException() {
        // Given / Arrange
        given(itemRepository.existsItemByNameAndType(anyString(), any(ItemType.class)))
                .willReturn(true);

        // When / Act
        var customException = assertThrows(CustomException.class, () -> itemService.createItem(originalItem));

        // Then / Assert
        verify(itemRepository, never()).save(originalItem);
        assertEquals(HttpStatus.BAD_REQUEST, customException.getHttpStatus());
        assertEquals(
                String.format("Already item with this name: %s, itemType: %s.", originalItem.getName(), originalItem.getType()),
                customException.getMessage()
        );
    }

    @Test
    void testGivenItem_whenUpdateItem_thenReturnUpdatedItem() {
        // Given / Arrange
        var itemId = UUID.randomUUID();
        originalItem.setId(itemId);
        var serviceItem = Item.builder()
                .name("Adm. Redes")
                .type(ItemType.SERVICE)
                .status(ItemStatus.DISABLED)
                .price(BigDecimal.valueOf(100.00))
                .build();

        given(itemRepository.findById(any(UUID.class))).willReturn(Optional.of(originalItem));
        given(itemRepository.existsItemByNameAndTypeAndIdNot(anyString(), any(ItemType.class), any(UUID.class)))
                .willReturn(false);
        given(itemRepository.save(originalItem)).willReturn(originalItem);

        // When / Act
        var updatedItem = itemService.updateItem(itemId, serviceItem);

        // Then / Assert
        verify(itemRepository, times(1)).save(originalItem);
        assertNotNull(updatedItem);
        assertEquals(serviceItem.getName(), updatedItem.getName());
        assertEquals(serviceItem.getType(), updatedItem.getType());
        assertEquals(serviceItem.getPrice(), updatedItem.getPrice());
        assertEquals(serviceItem.getStatus(), updatedItem.getStatus());
    }

    @Test
    void testGivenExistingItemName_whenUpdateItem_thenThrowsCustomException() {
        // Given / Arrange
        var itemId = UUID.randomUUID();
        originalItem.setId(itemId);
        var serviceItem = Item.builder()
                .name("Adm. Redes")
                .type(ItemType.SERVICE)
                .status(ItemStatus.DISABLED)
                .price(BigDecimal.valueOf(100.00))
                .build();

        given(itemRepository.findById(any(UUID.class))).willReturn(Optional.of(originalItem));
        given(itemRepository.existsItemByNameAndTypeAndIdNot(anyString(), any(ItemType.class), any(UUID.class)))
                .willReturn(true);

        // When / Act
        var customException = assertThrows(
                CustomException.class, () -> itemService.updateItem(itemId, serviceItem)
        );

        // Then / Assert
        verify(itemRepository, never()).save(originalItem);
        assertEquals(HttpStatus.BAD_REQUEST, customException.getHttpStatus());
        assertEquals(
                String.format("Already item with this name: %s, itemType: %s.", serviceItem.getName(), serviceItem.getType()),
                customException.getMessage()
        );
    }

    @Test
    void testGivenNonExistsItem_whenUpdateItem_thenThrowsCustomException() {
        // Given / Arrange
        var itemId = UUID.randomUUID();
        originalItem.setId(itemId);
        var serviceItem = Item.builder()
                .name("Adm. Redes")
                .type(ItemType.SERVICE)
                .status(ItemStatus.DISABLED)
                .price(BigDecimal.valueOf(100.00))
                .build();

        given(itemRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // When / Act
        var customException = assertThrows(
                CustomException.class, () -> itemService.updateItem(itemId, serviceItem)
        );

        // Then / Assert
        verify(itemRepository, never()).save(originalItem);
        assertEquals(HttpStatus.NOT_FOUND, customException.getHttpStatus());
        assertEquals(
                String.format("Cannot found item with id %s.", itemId),
                customException.getMessage()
        );
    }

    @Test
    void testGivenItemId_whenGetItemById_thenReturnFoundItem() {
        // Given / Arrange
        var itemId = UUID.randomUUID();
        given(itemRepository.findById(any(UUID.class))).willReturn(Optional.of(originalItem));

        // When / Act
        var foundItem = itemService.getItemById(itemId);

        // Then / Assert
        assertNotNull(foundItem);
        assertEquals("Ebook", foundItem.getName());
        assertEquals(ItemType.PRODUCT, foundItem.getType());
    }
}
