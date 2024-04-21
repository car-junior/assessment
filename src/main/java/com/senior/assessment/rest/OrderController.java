package com.senior.assessment.rest;

import com.senior.assessment.config.mapper.ModelMapperService;
import com.senior.assessment.domain.dto.PageResult;
import com.senior.assessment.domain.dto.order.OrderStatusChangeDto;
import com.senior.assessment.domain.dto.order.createupdate.OrderCreateUpdateDto;
import com.senior.assessment.domain.dto.order.detailslist.OrderDetailDto;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.enums.ItemType;
import com.senior.assessment.domain.enums.OrderStatus;
import com.senior.assessment.domain.querydsl.search.OrderSearch;
import com.senior.assessment.domain.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.senior.assessment.utilities.Utils.createPagination;

@RestController
@RequiredArgsConstructor
@RequestMapping("orders")
public class OrderController {
    private final OrderService orderService;
    private final ModelMapperService modelMapperService;

    @PostMapping
    public ResponseEntity<OrderDetailDto> create(@Valid @RequestBody OrderCreateUpdateDto orderCreate) {
        var order = orderService.createOrder(modelMapperService.toObject(Order.class, orderCreate));
        return ResponseEntity.ok(modelMapperService.toObject(OrderDetailDto.class, order));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDetailDto> update(@PathVariable(name = "orderId") UUID orderId,
                                                 @Valid @RequestBody OrderCreateUpdateDto orderUpdate) {
        var order = modelMapperService.toObject(Order.class, orderUpdate);
        return ResponseEntity.ok(
                modelMapperService.toObject(OrderDetailDto.class, orderService.updateOrder(orderId, order))
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailDto> getById(@PathVariable(name = "orderId") UUID orderId) {
        return ResponseEntity.ok(
                modelMapperService.toObject(OrderDetailDto.class, orderService.getOrderById(orderId))
        );
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteById(@PathVariable(name = "orderId") UUID orderId) {
        orderService.deleteOrderById(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PageResult<OrderDetailDto>> getAllOrder(
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ItemType itemType,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
            @RequestParam(required = false, defaultValue = "ASC") String sort,
            @RequestParam(required = false, defaultValue = "id") String sortName) {
        var orderSearch = OrderSearch.builder()
                .id(orderId)
                .query(query)
                .status(status)
                .itemType(itemType)
                .build();
        var pagination = createPagination(page, itemsPerPage, sort, sortName);
        var result = orderService.getAllOrder(orderSearch, pagination);
        return ResponseEntity.ok(modelMapperService.toPage(OrderDetailDto.class, result));
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<Void> deleteById(@PathVariable(name = "orderId") UUID orderId,
                                           @Valid @RequestBody OrderStatusChangeDto orderStatus) {
        orderService.updateStatus(orderId, orderStatus);
        return ResponseEntity.noContent().build();
    }
}
