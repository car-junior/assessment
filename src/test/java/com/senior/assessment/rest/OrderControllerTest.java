package com.senior.assessment.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senior.assessment.config.mapper.ModelMapperService;
import com.senior.assessment.domain.config.AssessmentConfigTest;
import com.senior.assessment.domain.dto.order.createupdate.OrderCreateUpdateDto;
import com.senior.assessment.domain.dto.order.createupdate.OrderItemDto;
import com.senior.assessment.domain.dto.order.detailslist.OrderDetailDto;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.service.OrderService;
import com.senior.assessment.infrastructure.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(AssessmentConfigTest.class)
@MockBean(JpaMetamodelMappingContext.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private OrderController orderController;

    @MockBean
    private ModelMapperService modelMapperService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGivenOrderCreateUpdateDto_whenCreateOrder_thenReturn200AndOrderDetailDetailDto() throws Exception {
        // Given / Arrange
        var itemDto = new OrderItemDto.ItemDto(UUID.randomUUID());
        var orderItem = OrderItemDto.builder()
                .amount(2)
                .item(itemDto)
                .build();
        var orderCreateDto = OrderCreateUpdateDto.builder()
                .discount(0.5)
                .orderItems(Set.of(orderItem))
                .build();
        var order = modelMapper.map(orderCreateDto, Order.class);
        order.setId(UUID.randomUUID());

        var orderDetailDto = modelMapper.map(order, OrderDetailDto.class);

        orderDetailDto.getOrderItems()
                .forEach(orderItemDto -> {
                    orderItemDto.getItem().setType(ItemType.PRODUCT);
                    orderItemDto.setItemPrice(BigDecimal.valueOf(50.00));
                });

        given(modelMapperService.toObject(eq(Order.class), any(OrderCreateUpdateDto.class)))
                .willReturn(order);
        given(orderService.createOrder(any(Order.class)))
                .willReturn(order);
        given(modelMapperService.toObject(eq(OrderDetailDto.class), any(Order.class)))
                .willReturn(orderDetailDto);

        // When / Act
        var response = mockMvc.perform(post("/orders")
                .content(objectMapper.writeValueAsString(orderCreateDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.discount").value(0))
                .andExpect(jsonPath("$.total").value(50.0))
                .andExpect(jsonPath("$.totalService").value(0))
                .andExpect(jsonPath("$.totalProduct").value(100.0))
                .andExpect(jsonPath("$.orderItems").isNotEmpty())
                .andExpect(jsonPath("$.orderItems[0].item").exists());
    }


}
