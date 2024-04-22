package com.senior.assessment.integration;

import com.senior.assessment.domain.config.AssessmentIntegrationConfig;
import com.senior.assessment.domain.dto.PageResult;
import com.senior.assessment.domain.dto.item.ItemCreateUpdateDto;
import com.senior.assessment.domain.dto.item.ItemDetailDto;
import com.senior.assessment.domain.dto.order.createupdate.OrderCreateUpdateDto;
import com.senior.assessment.domain.dto.order.createupdate.OrderItemDto;
import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
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
import java.util.Set;
import java.util.UUID;

import static com.senior.assessment.domain.config.ServerConfigTest.CONTENT_TYPE_JSON;
import static com.senior.assessment.domain.config.ServerConfigTest.SERVER_PORT;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ItemControllerIntegrationTest extends AssessmentIntegrationConfig {
    private static UUID itemId;
    private static ItemCreateUpdateDto itemCreateUpdateDto;
    private static RequestSpecification requestSpecification;

    @BeforeAll
    public static void setup() {
        requestSpecification = new RequestSpecBuilder()
                .setBasePath("/items")
                .setPort(SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        itemCreateUpdateDto = ItemCreateUpdateDto.builder()
                .name("Smartphone XYZ")
                .type(ItemType.PRODUCT)
                .status(ItemStatus.ACTIVE)
                .price(BigDecimal.valueOf(799.99))
                .build();
    }

    @Test
    @Order(1)
    void testGivenItemCreateUpdateDto_whenCreateItem_thenReturn200AndItemDetailDto() {
        var itemDetailDto = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(itemCreateUpdateDto)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ItemDetailDto.class);

        itemId = itemDetailDto.getId();

        assertNotNull(itemDetailDto);
        assertNotNull(itemDetailDto.getId());
        assertNotNull(itemDetailDto.getCreatedDate());
        assertNotNull(itemDetailDto.getLastModifiedDate());
        assertEquals(ItemStatus.ACTIVE, itemDetailDto.getStatus());
        assertEquals(itemCreateUpdateDto.getName(), itemDetailDto.getName());
        assertEquals(itemCreateUpdateDto.getType(), itemDetailDto.getType());
        assertThat(itemDetailDto.getPrice()).isEqualByComparingTo(itemCreateUpdateDto.getPrice());
    }

    @Test
    @Order(2)
    void testGivenItemCreateUpdateDto_whenUpdateItem_thenReturn200AndItemDetailDto() {
        itemCreateUpdateDto.setName("Manutenção Residencial");
        itemCreateUpdateDto.setType(ItemType.SERVICE);
        itemCreateUpdateDto.setStatus(ItemStatus.DISABLED);
        itemCreateUpdateDto.setPrice(BigDecimal.valueOf(1000.00));

        var itemDetailDto = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(itemCreateUpdateDto)
                .when()
                .put("/{itemId}", itemId)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ItemDetailDto.class);

        assertNotNull(itemDetailDto);
        assertNotNull(itemDetailDto.getId());
        assertNotNull(itemDetailDto.getCreatedDate());
        assertNotNull(itemDetailDto.getLastModifiedDate());
        assertEquals(itemId, itemDetailDto.getId());
        assertEquals(itemCreateUpdateDto.getName(), itemDetailDto.getName());
        assertEquals(itemCreateUpdateDto.getType(), itemDetailDto.getType());
        assertEquals(itemCreateUpdateDto.getStatus(), itemDetailDto.getStatus());
        assertThat(itemDetailDto.getPrice()).isEqualByComparingTo(itemCreateUpdateDto.getPrice());
    }

    @Test
    @Order(3)
    void testGivenItemId_whenGetItemById_thenReturn200AndItemDetailDto() {
        var itemDetailDto = given()
                .spec(requestSpecification)
                .when()
                .get("/{categoryId}", itemId)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ItemDetailDto.class);

        assertNotNull(itemDetailDto);
        assertNotNull(itemDetailDto.getId());
        assertNotNull(itemDetailDto.getCreatedDate());
        assertNotNull(itemDetailDto.getLastModifiedDate());
        assertEquals(itemId, itemDetailDto.getId());
        assertEquals(itemCreateUpdateDto.getName(), itemDetailDto.getName());
        assertEquals(itemCreateUpdateDto.getType(), itemDetailDto.getType());
        assertEquals(itemCreateUpdateDto.getStatus(), itemDetailDto.getStatus());
        assertThat(itemDetailDto.getPrice()).isEqualByComparingTo(itemCreateUpdateDto.getPrice());
    }

    @Test
    @Order(4)
    void testGivenEmptyItemSearchAndPaginationDefault_whenGetAllItem_thenReturn200AndPageResultItemDetailDto() {
        var newItemCreateDto = ItemCreateUpdateDto.builder()
                .name("Teclado")
                .type(ItemType.PRODUCT)
                .status(ItemStatus.ACTIVE)
                .price(BigDecimal.valueOf(100.00))
                .build();
        given().spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(newItemCreateDto)
                .when()
                .post()
                .then()
                .statusCode(200);

        PageResult<ItemDetailDto> pageResult = given()
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
        pageResult.getResult()
                .forEach(itemDetailDto -> {
                    assertNotNull(itemDetailDto.getId());
                    assertNotNull(itemDetailDto.getName());
                    assertNotNull(itemDetailDto.getType());
                    assertNotNull(itemDetailDto.getStatus());
                    assertNotNull(itemDetailDto.getCreatedDate());
                    assertNotNull(itemDetailDto.getLastModifiedDate());
                });
    }

    @Test
    @Order(5)
    void testGivenItemIdAndPaginationDefault_whenGetAllItem_thenReturn200AndPageResultItemDetailDto() {
        PageResult<ItemDetailDto> pageResult = given()
                .spec(requestSpecification)
                .param("itemId", itemId)
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
        assertNotNull(pageResult.getResult().get(0).getId());
        assertEquals(1, pageResult.getTotalPages());
        assertEquals(1, pageResult.getTotalResults());
        assertEquals(itemId, pageResult.getResult().get(0).getId());
    }

    @Test
    @Order(6)
    void testGivenQueryAndPaginationDefault_whenGetAllItem_thenReturn200AndPageResultItemDetailDto() {
        var query = "teclado";
        PageResult<ItemDetailDto> pageResult = given()
                .spec(requestSpecification)
                .param("query", query)
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
        pageResult.getResult()
                .forEach(itemDetailDto -> {
                    assertNotNull(itemDetailDto.getId());
                    assertNotNull(itemDetailDto.getName());
                    assertNotNull(itemDetailDto.getType());
                    assertNotNull(itemDetailDto.getStatus());
                    assertNotNull(itemDetailDto.getCreatedDate());
                    assertNotNull(itemDetailDto.getLastModifiedDate());
                });
    }

    @Test
    @Order(7)
    void testGivenItemTypeAndPaginationDefault_whenGetAllItem_thenReturn200AndPageResultItemDetailDto() {
        var type = ItemType.SERVICE;
        var newItemCreateDto = ItemCreateUpdateDto.builder()
                .name("Instalar Impressoras")
                .type(ItemType.SERVICE)
                .status(ItemStatus.ACTIVE)
                .price(BigDecimal.valueOf(50.00))
                .build();
        given().spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(newItemCreateDto)
                .when()
                .post()
                .then()
                .statusCode(200);

        PageResult<ItemDetailDto> pageResult = given()
                .spec(requestSpecification)
                .param("type", type)
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
        pageResult.getResult()
                .forEach(itemDetailDto -> {
                    assertNotNull(itemDetailDto.getId());
                    assertNotNull(itemDetailDto.getName());
                    assertNotNull(itemDetailDto.getType());
                    assertNotNull(itemDetailDto.getStatus());
                    assertNotNull(itemDetailDto.getCreatedDate());
                    assertNotNull(itemDetailDto.getLastModifiedDate());
                    assertEquals(type, itemDetailDto.getType());
                });
    }

    @Test
    @Order(8)
    void testGivenItemStatusAndPaginationDefault_whenGetAllItem_thenReturn200AndPageResultItemDetailDto() {
        var status = ItemStatus.DISABLED;
        var newItemCreateDto = ItemCreateUpdateDto.builder()
                .name("Mouse")
                .type(ItemType.PRODUCT)
                .status(ItemStatus.DISABLED)
                .price(BigDecimal.valueOf(50.00))
                .build();
        given().spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(newItemCreateDto)
                .when()
                .post()
                .then()
                .statusCode(200);

        PageResult<ItemDetailDto> pageResult = given()
                .spec(requestSpecification)
                .param("status", status)
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
        pageResult.getResult()
                .forEach(itemDetailDto -> {
                    assertNotNull(itemDetailDto.getId());
                    assertNotNull(itemDetailDto.getName());
                    assertNotNull(itemDetailDto.getType());
                    assertNotNull(itemDetailDto.getStatus());
                    assertNotNull(itemDetailDto.getCreatedDate());
                    assertNotNull(itemDetailDto.getLastModifiedDate());
                    assertEquals(status, itemDetailDto.getStatus());
                });
    }

    @Test
    @Order(9)
    void testGivenItemId_whenDeleteItemById_thenReturn204NoContent() {
        given().spec(requestSpecification)
                .when()
                .delete("/{itemId}", itemId)
                .then()
                .statusCode(204);
    }

    // Cenários negativos de teste

    @Test
    @Order(10)
    void testGivenItemIdLinkedWithOrder_whenDeleteItemById_thenReturn400AndErrorResponse() {
        var newItemCreateDto = ItemCreateUpdateDto.builder()
                .name("HeadSet")
                .type(ItemType.PRODUCT)
                .status(ItemStatus.ACTIVE)
                .price(BigDecimal.valueOf(100.00))
                .build();
        var itemDetailDto = given().spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(newItemCreateDto)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ItemDetailDto.class);

        var orderCreateDto = createOrderWithItem(itemDetailDto.getId());
        given().spec(new RequestSpecBuilder()
                        .setBasePath("/orders")
                        .setPort(SERVER_PORT)
                        .build())
                .contentType(CONTENT_TYPE_JSON)
                .body(orderCreateDto)
                .when()
                .post()
                .then()
                .statusCode(200);

        var errorResponse = given().spec(requestSpecification)
                .when()
                .delete("/{itemId}", itemDetailDto.getId())
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getStatus());
        assertEquals("Cannot delete item because have linked order.", errorResponse.getMessage());
    }

    @Test
    void testGivenInvalidItemCreateUpdateDto_whenCreateItem_thenReturn400AndErrorResponse() {
        var errorResponse = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(new ItemCreateUpdateDto())
                .when()
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getStatus());
        assertThat(errorResponse.getErrors()).isNotEmpty();
    }

    @Test
    void testGivenInvalidItemCreateUpdateDto_whenUpdateItem_thenReturn400AndErrorResponse() {
        var errorResponse = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(new ItemCreateUpdateDto())
                .when()
                .put("/{itemId}", UUID.randomUUID())
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
    void testGivenNonExistingItemId_whenUpdateItem_thenReturn404AndErrorResponse() {
        var nonExistingItemId = UUID.randomUUID();
        var itemUpdateDto = ItemCreateUpdateDto.builder()
                .name("Controle Universal")
                .type(ItemType.PRODUCT)
                .status(ItemStatus.ACTIVE)
                .price(BigDecimal.valueOf(29.99))
                .build();

        var errorResponse = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .body(itemUpdateDto)
                .when()
                .put("/{itemId}", nonExistingItemId)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getMessage());
        assertEquals(String.format("Cannot found item with id %s.", nonExistingItemId), errorResponse.getMessage());
    }

    @Test
    void testGivenNonExistingItemId_whenGetItemById_thenReturn404AndErrorResponse() {
        var nonExistingItemId = UUID.randomUUID();

        var errorResponse = given()
                .spec(requestSpecification)
                .contentType(CONTENT_TYPE_JSON)
                .when()
                .get("/{itemId}", nonExistingItemId)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getMessage());
        assertEquals(String.format("Cannot found item with id %s.", nonExistingItemId), errorResponse.getMessage());
    }

    private OrderCreateUpdateDto createOrderWithItem(UUID itemId) {
        var orderItem = OrderItemDto.builder()
                .amount(2)
                .item(new OrderItemDto.ItemDto(itemId))
                .build();
        return OrderCreateUpdateDto.builder()
                .orderItems(Set.of(orderItem))
                .build();
    }
}
