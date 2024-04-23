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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.senior.assessment.utilities.Utils.createPagination;

@RestController
@RequiredArgsConstructor
@RequestMapping("orders")
@Tag(name = "Pedidos",
        description = " Contém operações para Cadastrar, Listar, Editar e Excluir Pedidos e adicionar itens ao mesmo"
)
public class OrderController {
    private final OrderService orderService;
    private final ModelMapperService modelMapperService;

    @Operation(summary = "Operação para criar um order(Pedido).",
            description = "Olhe o schema OrderCreateUpdateDto para verificar as regras de cadastro. No cadastro de " +
                    "produtos são adicionados os itens, então itens pedidos são criados a partir de  pedidos"
    )
    @PostMapping
    public ResponseEntity<OrderDetailDto> create(@Valid @RequestBody OrderCreateUpdateDto orderCreate) {
        var order = orderService.createOrder(modelMapperService.toObject(Order.class, orderCreate));
        return ResponseEntity.ok(modelMapperService.toObject(OrderDetailDto.class, order));
    }

    @Operation(summary = "Operação para atualizar um order(Pedido) por ID.",
            description = "Olhe o schema OrderCreateUpdateDto para verificar as regras de cadastro. Pode ser " +
                    "adicionados itens ou removidos. Só é possível editar um pedido, inclusive aplicar desconto caso " +
                    "o pedido esteja em aberto(OPENED)."
    )
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDetailDto> update(@PathVariable(name = "orderId") UUID orderId,
                                                 @Valid @RequestBody OrderCreateUpdateDto orderUpdate) {
        var order = modelMapperService.toObject(Order.class, orderUpdate);
        return ResponseEntity.ok(
                modelMapperService.toObject(OrderDetailDto.class, orderService.updateOrder(orderId, order))
        );
    }

    @Operation(summary = "Operação para retornar um order(Pedido) por ID.")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailDto> getById(@PathVariable(name = "orderId") UUID orderId) {
        return ResponseEntity.ok(
                modelMapperService.toObject(OrderDetailDto.class, orderService.getOrderById(orderId))
        );
    }

    @Operation(summary = "Operação para deletar um order(Pedido) por ID.",
            description = "Só é possível deletar um pedido caso seu status esteja em aberto, pedidos fechados não são" +
                    " deletados."
    )
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteById(@PathVariable(name = "orderId") UUID orderId) {
        orderService.deleteOrderById(orderId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Operação para retornar uma página de orders(Pedidos).",
            description = "Neste endpoint é possível aplicar os filtros e mudar paginação.")
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

    @Operation(summary = "Operação para fechar(CLOSED) um order(pedido) por ID.",
            description = "Neste endpoint é possível fechar um pedido caso esteja em aberto.")
    @PatchMapping("/{orderId}")
    public ResponseEntity<Void> updateStatusById(@PathVariable(name = "orderId") UUID orderId,
                                                 @Valid @RequestBody OrderStatusChangeDto orderStatusDto) {
        orderService.updateStatus(orderId, orderStatusDto.getStatus());
        return ResponseEntity.noContent().build();
    }
}
