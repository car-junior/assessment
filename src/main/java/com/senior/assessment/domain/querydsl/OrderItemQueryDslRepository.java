package com.senior.assessment.domain.querydsl;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.entity.OrderItem;
import com.senior.assessment.domain.entity.QOrderItem;
import org.springframework.stereotype.Repository;

@Repository
public class OrderItemQueryDslRepository extends GenericQueryDslRepository<OrderItem, QOrderItem> {
    @Override
    protected QOrderItem getEntityPath() {
        return QOrderItem.orderItem;
    }
}