package com.senior.assessment.rest;

import com.senior.assessment.config.mapper.ModelMapperService;
import com.senior.assessment.domain.dto.order.OrderDetailDto;
import com.senior.assessment.domain.dto.order.createupdate.OrderCreateUpdateDto;
import com.senior.assessment.domain.entity.Order;
import com.senior.assessment.domain.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
//
//    @GetMapping
//    public ResponseEntity<PageResult<ItemDetailDto>> getAllItem(
//            @RequestParam(required = false) UUID itemId,
//            @RequestParam(required = false) String query,
//            @RequestParam(required = false) ItemType type,
//            @RequestParam(required = false) ItemStatus status,
//            @RequestParam(required = false, defaultValue = "0") int page,
//            @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
//            @RequestParam(required = false, defaultValue = "ASC") String sort,
//            @RequestParam(required = false, defaultValue = "id") String sortName) {
//        var itemSearch = ItemSearch.builder()
//                .id(itemId)
//                .query(query)
//                .type(type)
//                .status(status)
//                .build();
//        var pagination = createPagination(page, itemsPerPage, sort, sortName);
//        var result = itemService.getAllItem(itemSearch, pagination);
//        return ResponseEntity.ok(modelMapperService.toPage(ItemDetailDto.class, result));
//    }

}
