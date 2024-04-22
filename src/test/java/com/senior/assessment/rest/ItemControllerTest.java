package com.senior.assessment.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senior.assessment.config.mapper.ModelMapperService;
import com.senior.assessment.domain.config.AssessmentConfigTest;
import com.senior.assessment.domain.dto.PageResult;
import com.senior.assessment.domain.dto.item.ItemCreateUpdateDto;
import com.senior.assessment.domain.dto.item.ItemDetailDto;
import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.querydsl.search.ItemSearch;
import com.senior.assessment.domain.service.ItemService;
import com.senior.assessment.infrastructure.GlobalExceptionHandler;
import com.senior.assessment.infrastructure.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import(AssessmentConfigTest.class)
@MockBean(JpaMetamodelMappingContext.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ItemController itemController;

    @MockBean
    private ModelMapperService modelMapperService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGivenItemCreateUpdateDto_whenCreateItem_thenReturn200AndItemDetailDetailDto() throws Exception {
        // Given / Arrange
        var itemCreateDto = ItemCreateUpdateDto.builder()
                .name("Ebook")
                .type(ItemType.PRODUCT)
                .status(ItemStatus.ACTIVE)
                .price(BigDecimal.valueOf(50.00))
                .build();

        var item = modelMapper.map(itemCreateDto, Item.class);
        item.setId(UUID.randomUUID());

        given(modelMapperService.toObject(eq(Item.class), any(ItemCreateUpdateDto.class)))
                .willReturn(item);
        given(itemService.createItem(any(Item.class)))
                .willReturn(item);
        given(modelMapperService.toObject(eq(ItemDetailDto.class), any(Item.class)))
                .willReturn(modelMapper.map(item, ItemDetailDto.class));

        // When / Act
        var response = mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(itemCreateDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Ebook"))
                .andExpect(jsonPath("$.price").value(50.00))
                .andExpect(jsonPath("$.type").value(ItemType.PRODUCT.getCode()))
                .andExpect(jsonPath("$.status").value(ItemStatus.ACTIVE.getCode()));
    }

    @Test
    void testGivenInvalidItemCreateUpdateDto_whenCreateItem_thenReturn400AndErrors() throws Exception {
        // Given / Arrange

        // When / Act
        var response = mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(new ItemCreateUpdateDto()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.*").isNotEmpty());
    }

    @Test
    void testGivenItemCreateUpdateDto_whenUpdateItem_thenReturn200AndItemDetailDto() throws Exception {
        // Given / Arrange
        var itemId = UUID.randomUUID();
        var itemUpdateDto = ItemCreateUpdateDto.builder()
                .name("Monitor")
                .type(ItemType.PRODUCT)
                .status(ItemStatus.DISABLED)
                .price(BigDecimal.valueOf(100.00))
                .build();
        var item = modelMapper.map(itemUpdateDto, Item.class);
        item.setId(itemId);

        given(modelMapperService.toObject(eq(Item.class), any(ItemCreateUpdateDto.class)))
                .willReturn(item);
        given(itemService.updateItem(any(UUID.class), any(Item.class)))
                .willReturn(item);
        given(modelMapperService.toObject(eq(ItemDetailDto.class), any(Item.class)))
                .willReturn(modelMapper.map(item, ItemDetailDto.class));

        // When / Act
        var response = mockMvc.perform(put("/items/{itemId}", itemId)
                .content(objectMapper.writeValueAsString(itemUpdateDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Monitor"));
    }

    @Test
    void testGivenInvalidItemCreateUpdateDto_whenUpdateItem_thenReturn400AndErrors() throws Exception {
        // Given / Arrange

        // When / Act
        var response = mockMvc.perform(put("/items/{itemId}", UUID.randomUUID())
                .content(objectMapper.writeValueAsString(new ItemCreateUpdateDto()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.*").isNotEmpty());
    }

    @Test
    void testGivenNonExistsItemId_whenUpdateItem_thenReturn404AndErrorResponse() throws Exception {
        // Given / Arrange
        var itemId = UUID.randomUUID();
        var itemUpdateDto = ItemCreateUpdateDto.builder()
                .name("Monitor")
                .type(ItemType.PRODUCT)
                .status(ItemStatus.DISABLED)
                .price(BigDecimal.valueOf(100.00))
                .build();
        var item = modelMapper.map(itemUpdateDto, Item.class);

        given(modelMapperService.toObject(eq(Item.class), any(ItemCreateUpdateDto.class)))
                .willReturn(item);
        given(itemService.updateItem(any(UUID.class), any(Item.class)))
                .willThrow(CustomException.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .message(String.format("Cannot found item with id %s.", itemId))
                        .build());

        // When / Act
        var errorResponse = mockMvc.perform(put("/items/{itemId}", itemId)
                .content(objectMapper.writeValueAsString(itemUpdateDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        errorResponse.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format("Cannot found item with id %s.", itemId)));
    }

    @Test
    void testGivenItemId_whenGetItemById_thenReturn200AndItemDetailDto() throws Exception {
        // Given / Arrange
        var itemId = UUID.randomUUID();
        var item = Item.builder()
                .id(itemId)
                .type(ItemType.SERVICE)
                .status(ItemStatus.DISABLED)
                .name("Formatação Computadores")
                .price(BigDecimal.valueOf(100.00))
                .build();

        given(itemService.getItemById(any(UUID.class))).willReturn(item);
        given(modelMapperService.toObject(eq(ItemDetailDto.class), any(Item.class)))
                .willReturn(modelMapper.map(item, ItemDetailDto.class));

        // When / Act
        var response = mockMvc.perform(get("/items/{itemId}", itemId));

        //Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }
    @Test
    void testGivenNonExistingItemId_whenGetItemById_thenReturn400AndErrorResponse() throws Exception {
        // Given / Arrange
        var itemId = UUID.randomUUID();
        given(itemService.getItemById(any(UUID.class)))
                .willThrow(CustomException.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .message(String.format("Cannot found item with id %s.", itemId))
                        .build());

        // When / Act
        var errorResponse = mockMvc.perform(get("/items/{itemId}", itemId));

        //Then / Assert
        errorResponse.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format("Cannot found item with id %s.", itemId)));
    }

    @Test
    void testGivenItemId_whenDeleteItemById_thenReturn204() throws Exception {
        // Given / Arrange
        var itemId = UUID.randomUUID();
        willDoNothing().given(itemService).deleteItemById(itemId);

        // When / Act
        var response = mockMvc.perform(delete("/items/{itemId}", itemId));

        //Then / Assert
        response.andExpect(status().isNoContent());
    }

    @Test
    void testGivenNonExistingItemId_whenDeleteItemById_thenReturn400AndErrorResponse() throws Exception {
        // Given / Arrange
        var itemId = UUID.randomUUID();
        willThrow(CustomException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(String.format("Cannot found item with id %s.", itemId))
                .build())
                .given(itemService).deleteItemById(itemId);

        // When / Act
        var errorResponse = mockMvc.perform(delete("/items/{itemId}", itemId));

        //Then / Assert
        errorResponse.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format("Cannot found item with id %s.", itemId)));
    }

    @Test
    void testGivenItemSearchAndPagination_whenGetAllItem_thenReturn200AndPageResult() throws Exception {
        // Given / Arrange
        var items = createItems();
        var page = createPage(items);
        var pageResult = createPageResult(page);

        given(itemService.getAllItem(any(ItemSearch.class), any(PageRequest.class)))
                .willReturn(page);
        given(modelMapperService.toPage(eq(ItemDetailDto.class), any(Page.class)))
                .willReturn(pageResult);

        // When / Act
        var response = mockMvc.perform(get("/items"));

        //Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalResults").value(2))
                .andExpect(jsonPath("$.result").isNotEmpty());
    }

    @Test
    void testGivenItemSearchAndPagination_whenGetAllItem_thenReturn200AndEmptyPageResult() throws Exception {
        // Given / Arrange
        var itemPage = new PageImpl<Item>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        given(itemService.getAllItem(any(ItemSearch.class), any(PageRequest.class)))
                .willReturn(itemPage);
        given(modelMapperService.toPage(eq(ItemDetailDto.class), any(Page.class)))
                .willReturn(new PageResult<>(0, 0, Collections.emptyList()));

        // When / Act
        var response = mockMvc.perform(get("/items"));

        // Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.totalResults").value(0))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    private List<Item> createItems() {
        return List.of(Item.builder()
                        .name("Ryzen 7")
                        .type(ItemType.PRODUCT)
                        .price(BigDecimal.valueOf(500.00))
                        .build(),
                Item.builder()
                        .name("Formatar Computador")
                        .type(ItemType.SERVICE)
                        .price(BigDecimal.valueOf(100.00))
                        .build()
        );
    }

    private Page<Item> createPage(List<Item> items) {
        return new PageImpl<>(items, PageRequest.of(0, 10), items.size());
    }

    private PageResult<ItemDetailDto> createPageResult(Page<Item> page) {
        List<ItemDetailDto> itemsDetailDto = page.getContent()
                .stream()
                .map(item -> modelMapper.map(item, ItemDetailDto.class))
                .toList();
        return new PageResult<>(page.getTotalPages(), page.getTotalElements(), itemsDetailDto);
    }
}
