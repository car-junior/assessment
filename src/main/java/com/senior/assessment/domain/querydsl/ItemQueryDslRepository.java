package com.senior.assessment.domain.querydsl;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.entity.OrderItem;
import com.senior.assessment.domain.entity.QItem;
import com.senior.assessment.domain.entity.QOrderItem;
import org.springframework.stereotype.Repository;

@Repository
public class ItemQueryDslRepository extends GenericQueryDslRepository<Item, QItem> {
    @Override
    protected QItem getEntityPath() {
        return QItem.item;
    }
}