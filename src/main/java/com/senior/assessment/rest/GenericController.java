package com.senior.assessment.rest;

import com.senior.assessment.domain.querydsl.ItemQueryDslRepository;
import com.senior.assessment.domain.querydsl.OrderItemQueryDslRepository;
import com.senior.assessment.domain.querydsl.RelatorioItemQueryDslRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("generics")
@RequiredArgsConstructor
public class GenericController {
    private final OrderItemQueryDslRepository orderItemQueryDslRepository;
    private final ItemQueryDslRepository itemQueryDslRepository;
    private final RelatorioItemQueryDslRepository relatorioItemQueryDslRepository;

    @GetMapping("item-orders")
    public ResponseEntity<Object> itemsOrders() {
//        itemQueryDslRepository.findAll();
        relatorioItemQueryDslRepository.findAll();
        return ResponseEntity.ok(orderItemQueryDslRepository.findAll());
    }
}
