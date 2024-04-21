package com.senior.assessment.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senior.assessment.config.mapper.ModelMapperService;
import com.senior.assessment.domain.dto.item.ItemCreateUpdateDto;
import com.senior.assessment.domain.dto.item.ItemDetailDto;
import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.service.ItemService;
import com.senior.assessment.infrastructure.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ItemController itemController;

    @MockBean
    private ModelMapperService modelMapperService;

    private final ModelMapper modelMapper = new ModelMapper();

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
                .content(new ObjectMapper().writeValueAsString(itemCreateDto))
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
}
