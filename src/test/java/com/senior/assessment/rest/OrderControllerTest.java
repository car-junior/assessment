package com.senior.assessment.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senior.assessment.config.mapper.ModelMapperService;
import com.senior.assessment.domain.config.AssessmentConfigTest;
import com.senior.assessment.domain.dto.order.createupdate.OrderCreateUpdateDto;
import com.senior.assessment.domain.dto.order.createupdate.OrderItemDto;
import com.senior.assessment.domain.dto.order.detailslist.OrderDetailDto;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.enums.OrderStatus;
import com.senior.assessment.domain.service.OrderService;
import com.senior.assessment.infrastructure.GlobalExceptionHandler;
import com.senior.assessment.infrastructure.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private OrderCreateUpdateDto orderCreateUpdateDto;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        orderCreateUpdateDto = getOrderCreateUpdateDto();
    }

    @Test
    void testGivenOrderCreateUpdateDto_whenCreateOrder_thenReturn200AndOrderDetailDetailDto() throws Exception {
        // Given / Arrange
        var order = modelMapper.map(orderCreateUpdateDto, Order.class);
        order.setId(UUID.randomUUID());
        var orderDetailDto = getOrderDetailDto(order);

        given(modelMapperService.toObject(eq(Order.class), any(OrderCreateUpdateDto.class)))
                .willReturn(order);
        given(orderService.createOrder(any(Order.class)))
                .willReturn(order);
        given(modelMapperService.toObject(eq(OrderDetailDto.class), any(Order.class)))
                .willReturn(orderDetailDto);

        // When / Act
        var response = mockMvc.perform(post("/orders")
                .content(objectMapper.writeValueAsString(orderCreateUpdateDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.discount").value(0))
                .andExpect(jsonPath("$.total").value(250.00))
                .andExpect(jsonPath("$.totalService").value(200.00))
                .andExpect(jsonPath("$.totalProduct").value(50.00))
                .andExpect(jsonPath("$.orderItems").isNotEmpty());
    }

    @Test
    void testGivenInvalidOrderCreateUpdateDto_whenCreateOrder_thenReturn400AndErrors() throws Exception {
        // Given / Arrange

        // When / Act
        var response = mockMvc.perform(post("/orders")
                .content(objectMapper.writeValueAsString(new OrderCreateUpdateDto()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.*").isNotEmpty());
    }

    @Test
    void testGivenOrderUpdateCreateDto_whenUpdateOrder_thenReturn200AndOrderDetailDto() throws Exception {
        // Given / Arrange
        var orderId = UUID.randomUUID();
        orderCreateUpdateDto.setDiscount(0.2);
        var order = modelMapper.map(orderCreateUpdateDto, Order.class);
        order.setId(orderId);

        given(modelMapperService.toObject(eq(Order.class), any(OrderCreateUpdateDto.class)))
                .willReturn(order);
        given(orderService.updateOrder(any(UUID.class), any(Order.class)))
                .willReturn(order);
        given(modelMapperService.toObject(eq(OrderDetailDto.class), any(Order.class)))
                .willReturn(getOrderDetailDto(order));

        // When / Act
        var response = mockMvc.perform(put("/orders/{orderId}", orderId)
                .content(objectMapper.writeValueAsString(orderCreateUpdateDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.discount").value(0.2))
                .andExpect(jsonPath("$.total").value(280.00))
                .andExpect(jsonPath("$.totalService").value(200.00))
                .andExpect(jsonPath("$.totalProduct").value(80.00))
                .andExpect(jsonPath("$.orderItems").isNotEmpty());
    }

    @Test
    void testGivenInvalidOrderCreateUpdateDto_whenUpdateOrder_thenReturn400AndErrors() throws Exception {
        // Given / Arrange

        // When / Act
        var response = mockMvc.perform(put("/orders/{itemId}", UUID.randomUUID())
                .content(objectMapper.writeValueAsString(new OrderCreateUpdateDto()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.*").isNotEmpty());
    }

    @Test
    void testGivenNonExistsOrderId_whenUpdateOrder_thenReturn404AndErrorResponse() throws Exception {
        // Given / Arrange
        var orderId = UUID.randomUUID();
        var order = modelMapper.map(orderCreateUpdateDto, Order.class);

        given(modelMapperService.toObject(eq(Order.class), any(OrderCreateUpdateDto.class)))
                .willReturn(order);
        given(orderService.updateOrder(any(UUID.class), any(Order.class)))
                .willThrow(CustomException.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .message(String.format("Cannot found order with id %s.", orderId))
                        .build());

        // When / Act
        var errorResponse = mockMvc.perform(put("/orders/{orderId}", orderId)
                .content(objectMapper.writeValueAsString(orderCreateUpdateDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        errorResponse.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format("Cannot found order with id %s.", orderId)));
    }

    @Test
    void testGivenOrderIdClosed_whenUpdateOrder_thenReturn400AndErrorResponse() throws Exception {
        // Given / Arrange
        var orderId = UUID.randomUUID();
        var order = modelMapper.map(orderCreateUpdateDto, Order.class);

        given(modelMapperService.toObject(eq(Order.class), any(OrderCreateUpdateDto.class)))
                .willReturn(order);
        given(orderService.updateOrder(any(UUID.class), any(Order.class)))
                .willThrow(CustomException.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .message(String.format("Cannot edit order because is %s.", OrderStatus.CLOSED))
                        .build());

        // When / Act
        var errorResponse = mockMvc.perform(put("/orders/{orderId}", orderId)
                .content(objectMapper.writeValueAsString(orderCreateUpdateDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //Then / Assert
        errorResponse.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value(String.format("Cannot edit order because is %s.", OrderStatus.CLOSED)));
    }

    @Test
    void testGivenNonExistsOrderId_whenDeleteOrderById_thenReturn404AndErrorResponse() throws Exception {
        // Given / Arrange
        var orderId = UUID.randomUUID();

        willThrow(CustomException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(String.format("Cannot found order with id %s.", orderId))
                .build())
                .given(orderService).deleteOrderById(orderId);

        // When / Act
        var errorResponse = mockMvc.perform(delete("/orders/{orderId}", orderId));

        //Then / Assert
        errorResponse.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value(String.format("Cannot found order with id %s.", orderId)));
    }

    @Test
    void testGivenOrderIdClosed_whenDeleteOrderById_thenReturn400AndErrorResponse() throws Exception {
        // Given / Arrange
        var orderId = UUID.randomUUID();

        willThrow(CustomException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(String.format("Cannot delete order %s.", OrderStatus.CLOSED))
                .build())
                .given(orderService).deleteOrderById(orderId);

        // When / Act
        var errorResponse = mockMvc.perform(delete("/orders/{orderId}", orderId));

        //Then / Assert
        errorResponse.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(String.format("Cannot delete order %s.", OrderStatus.CLOSED)));
    }

    private OrderDetailDto getOrderDetailDto(Order order) {
        var orderDetailDto = modelMapper.map(order, OrderDetailDto.class);
        IntStream.rangeClosed(0, orderDetailDto.getOrderItems().size() - 1)
                .forEach(index -> {
                    var orderItemDetailDto = orderDetailDto.getOrderItems().get(index);
                    if (index % 2 == 0) {
                        orderItemDetailDto.getItem().setType(ItemType.PRODUCT);
                        orderItemDetailDto.setItemPrice(BigDecimal.valueOf(50.00));
                    }
                    else {
                        orderItemDetailDto.getItem().setType(ItemType.SERVICE);
                        orderItemDetailDto.setItemPrice(BigDecimal.valueOf(100.00));
                    }
                });
        return orderDetailDto;
    }
    private OrderCreateUpdateDto getOrderCreateUpdateDto() {
        var orderItemsDto = new HashSet<OrderItemDto>();
        IntStream.rangeClosed(0, 1)
                .forEach(index -> orderItemsDto.add(OrderItemDto.builder()
                        .amount(2)
                        .item(new OrderItemDto.ItemDto(UUID.randomUUID()))
                        .build())
                );
        return OrderCreateUpdateDto.builder()
                .discount(0.5)
                .orderItems(orderItemsDto)
                .build();
    }
}
