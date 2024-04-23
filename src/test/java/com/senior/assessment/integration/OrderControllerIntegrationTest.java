package com.senior.assessment.integration;

import com.senior.assessment.domain.config.PostgreSQLContainerConfig;
import com.senior.assessment.domain.dto.PageResult;
import com.senior.assessment.domain.dto.item.ItemCreateUpdateDto;
import com.senior.assessment.domain.dto.item.ItemDetailDto;
import com.senior.assessment.domain.dto.order.OrderStatusChangeDto;
import com.senior.assessment.domain.dto.order.createupdate.OrderCreateUpdateDto;
import com.senior.assessment.domain.dto.order.createupdate.OrderItemDto;
import com.senior.assessment.domain.dto.order.detailslist.OrderDetailDto;
import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.enums.OrderStatus;
import com.senior.assessment.infrastructure.ErrorResponse;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.senior.assessment.domain.config.ServerConfigTest.CONTENT_TYPE_JSON;
import static com.senior.assessment.domain.config.ServerConfigTest.SERVER_PORT;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class OrderControllerIntegrationTest extends PostgreSQLContainerConfig {
    private static UUID orderOneId;
    private static UUID orderTwoId;
    private static OrderCreateUpdateDto orderCreateUpdateDto;
    private static RequestSpecification requestSpecification;
    private static RequestSpecification requestSpecificationItem;
    private final static List<ItemDetailDto> itemsDetailDto = new ArrayList<>();

    @BeforeAll
    public static void setup() {
        requestSpecification = new RequestSpecBuilder()
                .setBasePath("/orders")
                .setPort(SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
        requestSpecificationItem = new RequestSpecBuilder()
                .setBasePath("/items")
                .setPort(SERVER_PORT)
                .build();
    }

    @Test
    @Order(1)
    void testGivenOrderCreateUpdateDto_whenCreateOrder_thenReturn200AndOrderDetailDto() {
        createItems();
        orderCreateUpdateDto = getOrderWithProductAndService();

        var orderDetailDto = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(orderCreateUpdateDto)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OrderDetailDto.class);

        orderOneId = orderDetailDto.getId();

        assertNotNull(orderDetailDto);
        assertNotNull(orderDetailDto.getId());
        assertThat(orderDetailDto.getTotal()).isEqualByComparingTo("960.00");
        assertThat(orderDetailDto.getTotalProduct()).isEqualByComparingTo("160.00");
        assertThat(orderDetailDto.getTotalService()).isEqualByComparingTo("800.00");
        assertThat(orderDetailDto.getOrderItems()).isNotEmpty();
        orderDetailDto.getOrderItems()
                .forEach(orderItemDetailDto -> {
                    assertNotNull(orderItemDetailDto.getId());
                    assertNotNull(orderItemDetailDto.getItem());
                    assertNotNull(orderItemDetailDto.getItem().getId());
                    assertNotNull(orderItemDetailDto.getItem().getName());
                    assertNotNull(orderItemDetailDto.getItem().getType());
                    assertNotNull(orderItemDetailDto.getCreatedDate());
                    assertNotNull(orderItemDetailDto.getLastModifiedDate());
                    assertThat(orderItemDetailDto.getAmount()).isGreaterThanOrEqualTo(1);
                    assertThat(orderItemDetailDto.getItemPrice()).isGreaterThan(BigDecimal.valueOf(0.01));
                    assertThat(orderItemDetailDto.getItemPrice()).isGreaterThan(BigDecimal.valueOf(0.01));
                });
    }

    @Test
    @Order(2)
    void testGivenOrderCreateUpdateDto_whenUpdateOrder_thenReturn200AndOrderDetailDto() {
        orderCreateUpdateDto.setDiscount(0.5);
        orderCreateUpdateDto.getOrderItems().forEach(orderItemDto -> orderItemDto.setAmount(2));

        var orderDetailDto = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(orderCreateUpdateDto)
                .when()
                .put("/{orderId}", orderOneId)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OrderDetailDto.class);

        assertNotNull(orderDetailDto);
        assertNotNull(orderDetailDto.getId());
        assertThat(orderDetailDto.getTotal()).isEqualByComparingTo("500.00");
        assertThat(orderDetailDto.getTotalProduct()).isEqualByComparingTo("100.00");
        assertThat(orderDetailDto.getTotalService()).isEqualByComparingTo("400.00");
        assertThat(orderDetailDto.getOrderItems()).isNotEmpty();
        orderDetailDto.getOrderItems()
                .forEach(orderItemDetailDto -> {
                    assertNotNull(orderItemDetailDto.getId());
                    assertNotNull(orderItemDetailDto.getItem());
                    assertNotNull(orderItemDetailDto.getItem().getId());
                    assertNotNull(orderItemDetailDto.getItem().getName());
                    assertNotNull(orderItemDetailDto.getItem().getType());
                    assertNotNull(orderItemDetailDto.getCreatedDate());
                    assertNotNull(orderItemDetailDto.getLastModifiedDate());
                    assertEquals(2, orderItemDetailDto.getAmount());
                    assertThat(orderItemDetailDto.getItemPrice()).isGreaterThan(BigDecimal.valueOf(0.01));
                    assertThat(orderItemDetailDto.getItemPrice()).isGreaterThan(BigDecimal.valueOf(0.01));
                });
    }

    @Test
    @Order(3)
    void testGivenOrderId_whenGetOrderById_thenReturn200AndOrderDetailDto() {
        var orderDetailDto = given()
                .spec(requestSpecification)
                .when()
                .get("/{orderId}", orderOneId)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OrderDetailDto.class);

        assertNotNull(orderDetailDto);
        assertNotNull(orderDetailDto.getId());
        assertThat(orderDetailDto.getTotal()).isEqualByComparingTo("500.00");
        assertThat(orderDetailDto.getTotalProduct()).isEqualByComparingTo("100.00");
        assertThat(orderDetailDto.getTotalService()).isEqualByComparingTo("400.00");
        assertThat(orderDetailDto.getOrderItems()).isNotEmpty();
        orderDetailDto.getOrderItems()
                .forEach(orderItemDetailDto -> {
                    assertNotNull(orderItemDetailDto.getId());
                    assertNotNull(orderItemDetailDto.getItem());
                    assertNotNull(orderItemDetailDto.getItem().getId());
                    assertNotNull(orderItemDetailDto.getItem().getName());
                    assertNotNull(orderItemDetailDto.getItem().getType());
                    assertNotNull(orderItemDetailDto.getCreatedDate());
                    assertNotNull(orderItemDetailDto.getLastModifiedDate());
                    assertEquals(2, orderItemDetailDto.getAmount());
                    assertThat(orderItemDetailDto.getItemPrice()).isGreaterThan(BigDecimal.valueOf(0.01));
                    assertThat(orderItemDetailDto.getItemPrice()).isGreaterThan(BigDecimal.valueOf(0.01));
                });
    }

    @Test
    @Order(4)
    void testGivenOrderIdAndOrderStatus_whenUpdateStatusById_thenReturn204NoContent() {
        var orderStatusChange = new OrderStatusChangeDto(OrderStatus.CLOSED);
        given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(orderStatusChange)
                .when()
                .patch("/{orderId}", orderOneId)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    void testGivenEmptyOrderSearchAndPaginationDefault_whenGetAllOrder_thenReturn200AndPageResultOrderDetailDto() {

        var newOrderCreateUpdateDto = getOrderWithProductAndService();
        newOrderCreateUpdateDto.setDiscount(0);

        orderTwoId = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(newOrderCreateUpdateDto)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OrderDetailDto.class)
                .getId();

        PageResult<OrderDetailDto> pageResult = given()
                .spec(requestSpecification)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<>() {
                });

        assertNotNull(pageResult);
        assertNotNull(pageResult.getResult());
        assertEquals(1, pageResult.getTotalPages());
        assertEquals(2, pageResult.getTotalResults());
    }

    @Test
    @Order(6)
    void testGivenOrderIdAndPaginationDefault_whenGetAllOrder_thenReturn200AndPageResultOrderDetailDto() {
        PageResult<OrderDetailDto> pageResult = given()
                .spec(requestSpecification)
                .param("orderId", orderOneId)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<>() {
                });

        assertNotNull(pageResult);
        assertNotNull(pageResult.getResult());
        assertEquals(1, pageResult.getTotalPages());
        assertEquals(1, pageResult.getTotalResults());
        assertEquals(orderOneId, pageResult.getResult().get(0).getId());
        assertEquals(OrderStatus.CLOSED, pageResult.getResult().get(0).getStatus());
        assertThat(pageResult.getResult().get(0).getTotal()).isEqualByComparingTo("500.00");
        assertThat(pageResult.getResult().get(0).getTotalProduct()).isEqualByComparingTo("100.00");
        assertThat(pageResult.getResult().get(0).getTotalService()).isEqualByComparingTo("400.00");
    }

    @Test
    @Order(7)
    void testGivenOrderStatusAndPaginationDefault_whenGetAllOrder_thenReturn200AndPageResultOrderDetailDto() {
        PageResult<OrderDetailDto> pageResult = given()
                .spec(requestSpecification)
                .param("status", OrderStatus.OPENED)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<>() {
                });

        assertNotNull(pageResult);
        assertNotNull(pageResult.getResult());
        assertEquals(1, pageResult.getTotalPages());
        assertEquals(1, pageResult.getTotalResults());
        assertEquals(OrderStatus.OPENED, pageResult.getResult().get(0).getStatus());
    }

    @Test
    @Order(8)
    void testGivenItemTypeAndPaginationDefault_whenGetAllOrder_thenReturn200AndPageResultOrderDetailDto() {
        PageResult<OrderDetailDto> pageResult = given()
                .spec(requestSpecification)
                .param("itemType", ItemType.PRODUCT)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<>() {
                });

        assertNotNull(pageResult);
        assertNotNull(pageResult.getResult());
        assertEquals(1, pageResult.getTotalPages());
        assertEquals(2, pageResult.getTotalResults());
    }

    @Test
    @Order(9)
    void testGivenQueryAndPaginationDefault_whenGetAllOrder_thenReturn200AndPageResultOrderDetailDto() {
        PageResult<OrderDetailDto> pageResult = given()
                .spec(requestSpecification)
                .param("query", "xyz")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<>() {
                });

        assertNotNull(pageResult);
        assertNotNull(pageResult.getResult());
        assertEquals(1, pageResult.getTotalPages());
        assertEquals(2, pageResult.getTotalResults());
    }

    // Testes cenários negativos

    @Test
    @Order(10)
    void testGivenInvalidOrderCreateUpdateDto_whenCreateOrder_thenReturn400AndErrorResponse() {
        var errorResponse = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(new OrderCreateUpdateDto())
                .when()
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getErrors());
        assertThat(errorResponse.getErrors()).isNotEmpty();
    }

    @Test
    @Order(11)
    void testGivenOrderCreateUpdateDtoWithNonExistsItemsIds_whenCreateOrder_thenReturn404AndErrorResponse() {
        var orderCreateDtoNonExistsItems = getOrderWithProductAndService();

        // Adiciona ids inexistentes para items
        orderCreateDtoNonExistsItems.getOrderItems()
                .forEach(orderItemDto -> orderItemDto.getItem().setId(UUID.randomUUID()));

        var errorResponse = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(orderCreateDtoNonExistsItems)
                .when()
                .post()
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getMessage());
    }

    @Test
    @Order(12)
    void testGivenOrderCreateUpdateDtoWithProductItemDisable_whenCreateOrder_thenReturn400AndErrorResponse() {
        // Cria um PRODUCT item DISABLED e adiciona ao pedido
        var productDisabled = itemsDetailDto.stream()
                .filter(itemDetailDto -> itemDetailDto.getType() == ItemType.PRODUCT &&
                        itemDetailDto.getStatus() == ItemStatus.DISABLED)
                .findFirst()
                .orElseGet(ItemDetailDto::new);

        var orderCreateDto = getOrderWithProductAndService();
        var newOrderItem = OrderItemDto.builder()
                .amount(2)
                .item(new OrderItemDto.ItemDto(productDisabled.getId()))
                .build();
        orderCreateDto.getOrderItems().add(newOrderItem);

        var errorResponse = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(orderCreateDto)
                .when()
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getMessage());
    }

    @Test
    @Order(13)
    void testGivenDiscountToOrderDisabled_whenUpdateOrder_thenReturn400AndErrorResponse() {
        orderCreateUpdateDto.setDiscount(0.8);

        var errorResponse = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(orderCreateUpdateDto)
                .when()
                .put("/{orderId}", orderOneId)
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getMessage());
    }

    @Test
    @Order(14)
    void testGivenOrderIdClosed_whenDeleteOrderById_thenReturn400AndErrorResponse() {
        var errorResponse = given()
                .spec(requestSpecification)
                .when()
                .delete("/{orderId}", orderOneId)
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getMessage());
    }

    @Test
    @Order(15)
    void testGivenOrderId_whenDeleteOrderById_thenReturn204NonContent() {
        given()
                .spec(requestSpecification)
                .when()
                .delete("/{orderId}", orderTwoId)
                .then()
                .statusCode(204);
    }

    private OrderCreateUpdateDto getOrderWithProductAndService() {
        var productActive = itemsDetailDto.stream()
                .filter(itemDetailDto -> itemDetailDto.getType() == ItemType.PRODUCT &&
                        itemDetailDto.getStatus() == ItemStatus.ACTIVE)
                .findFirst()
                .orElseGet(ItemDetailDto::new);
        var service = itemsDetailDto.stream()
                .filter(itemDetailDto -> itemDetailDto.getType() == ItemType.SERVICE &&
                        itemDetailDto.getStatus() == ItemStatus.ACTIVE)
                .findFirst()
                .orElseGet(ItemDetailDto::new);

        var itemOne = new OrderItemDto.ItemDto(productActive.getId());
        var itemTwo = new OrderItemDto.ItemDto(service.getId());
        var orders = new HashSet<OrderItemDto>();

        orders.add(OrderItemDto.builder()
                .amount(2)
                .item(itemOne)
                .build());
        orders.add(OrderItemDto.builder()
                .amount(4)
                .item(itemTwo)
                .build());

        return OrderCreateUpdateDto.builder()
                .discount(0.2)
                .orderItems(orders)
                .build();
    }

    private void createItems() {
        var itemOne = ItemCreateUpdateDto.builder()
                .name("Smartphone XYZ")
                .type(ItemType.PRODUCT)
                .status(ItemStatus.ACTIVE)
                .price(BigDecimal.valueOf(100.00))
                .build();

        var itemTwo = ItemCreateUpdateDto.builder()
                .name("Formatar Computadores")
                .type(ItemType.SERVICE)
                .status(ItemStatus.ACTIVE)
                .price(BigDecimal.valueOf(200.00))
                .build();

        var itemThree = ItemCreateUpdateDto.builder()
                .name("Teclado Gamer")
                .type(ItemType.PRODUCT)
                .status(ItemStatus.DISABLED)
                .price(BigDecimal.valueOf(25.00))
                .build();

        itemsDetailDto.add(given()
                .spec(requestSpecificationItem)
                .contentType(CONTENT_TYPE_JSON)
                .body(itemOne)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ItemDetailDto.class)
        );

        itemsDetailDto.add(given()
                .spec(requestSpecificationItem)
                .contentType(CONTENT_TYPE_JSON)
                .body(itemTwo)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ItemDetailDto.class)
        );

        itemsDetailDto.add(given()
                .spec(requestSpecificationItem)
                .contentType(CONTENT_TYPE_JSON)
                .body(itemThree)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ItemDetailDto.class)
        );
    }
}
