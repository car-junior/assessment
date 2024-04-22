package com.senior.assessment.integration;

import com.senior.assessment.domain.config.AssessmentIntegrationConfig;
import com.senior.assessment.domain.dto.PageResult;
import com.senior.assessment.domain.dto.item.ItemCreateUpdateDto;
import com.senior.assessment.domain.dto.item.ItemDetailDto;
import com.senior.assessment.domain.enums.ItemStatus;
import com.senior.assessment.domain.enums.ItemType;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
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
    void testGivenEmptyItemSearchAndPaginationDefault_whenGetAllItem_thenReturnPageResultItemDetailDto() {
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
}
