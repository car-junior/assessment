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

    private Item item;


    @BeforeEach
    public void setup() {
        item = Item.builder()
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

        given(itemRepository.save(item)).willReturn(item);

        // When / Act
        var savedItem = itemService.createItem(item);

        // Then / Assert
        verify(itemRepository, times(1)).save(item);
        assertNotNull(savedItem);
    }

    @Test
    void testGivenExistingItemName_whenCreateItem_thenThrowsCustomException() {
        // Given / Arrange
        given(itemRepository.existsItemByNameAndType(anyString(), any(ItemType.class)))
                .willReturn(true);

        // When / Act
        var customException = assertThrows(CustomException.class, () -> itemService.createItem(item));

        // Then / Assert
        verify(itemRepository, never()).save(item);
        assertEquals(HttpStatus.BAD_REQUEST, customException.getHttpStatus());
        assertEquals(
                String.format("Already item with this name: %s, itemType: %s.", item.getName(), item.getType()),
                customException.getMessage()
        );
    }

    @Test
    void testGivenItem_whenUpdateItem_thenReturnUpdatedItem() {
        // Given / Arrange
        var itemId = UUID.randomUUID();
        item.setId(itemId);
        var serviceItem = Item.builder()
                .name("Adm. Redes")
                .type(ItemType.SERVICE)
                .status(ItemStatus.DISABLED)
                .price(BigDecimal.valueOf(100.00))
                .build();

        given(itemRepository.findById(any(UUID.class))).willReturn(Optional.of(item));
        given(itemRepository.existsItemByNameAndTypeAndIdNot(anyString(), any(ItemType.class), any(UUID.class)))
                .willReturn(false);
        given(itemRepository.save(item)).willReturn(item);

        // When / Act
        var updatedItem = itemService.updateItem(itemId, serviceItem);

        // Then / Assert
        verify(itemRepository, times(1)).save(item);
        assertNotNull(updatedItem);
        assertEquals(serviceItem.getName(), updatedItem.getName());
        assertEquals(serviceItem.getType(), updatedItem.getType());
        assertEquals(serviceItem.getPrice(), updatedItem.getPrice());
        assertEquals(serviceItem.getStatus(), updatedItem.getStatus());
    }
}
