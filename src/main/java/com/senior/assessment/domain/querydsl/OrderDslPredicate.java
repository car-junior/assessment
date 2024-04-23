package com.senior.assessment.domain.querydsl;

import com.querydsl.core.types.Predicate;
import com.senior.assessment.domain.entity.QOrder;
import com.senior.assessment.domain.querydsl.search.OrderSearch;
import org.springframework.stereotype.Component;

import static com.senior.assessment.utilities.Utils.*;

@Component
public class OrderDslPredicate {

    private OrderDslPredicate() {
    }

    public Predicate expression(OrderSearch orderSearch) {
        var order = QOrder.order;
        var predicate = order.id.isNotNull();

        if (isPresent(orderSearch.query())) {
            var query = "%" + unaccented(orderSearch.query()) + "%";
            predicate = predicate.and(unaccentedExpression(order.orderItems.any().item.name).likeIgnoreCase(query));
        }

        if (isPresent(orderSearch.id()))
            predicate = predicate.and(order.id.eq(orderSearch.id()));

        if (isPresent(orderSearch.status()))
            predicate = predicate.and(order.status.eq(orderSearch.status()));

        if (isPresent(orderSearch.itemType()))
            predicate = predicate.and(order.orderItems.any().item.type.eq(orderSearch.itemType()));

        return predicate;
    }
}
