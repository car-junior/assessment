package com.senior.assessment.domain.querydsl.search;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.senior.assessment.domain.entity.QItem;

import static com.senior.assessment.utilities.Utils.isPresent;

public class ItemDslPredicate {

    private ItemDslPredicate() {}

    public static Predicate expression(ItemSearch itemSearch) {
        var qItem = QItem.item;
        var predicate = qItem.id.isNotNull();

        //TODO: Depois retirar acentuação
        if (isPresent(itemSearch.query()))
            predicate = predicate.and(qItem.name.likeIgnoreCase("%".concat(itemSearch.query()).concat("%")));

        if (isPresent(itemSearch.id()))
            predicate = predicate.and(qItem.id.eq(itemSearch.id()));

        if (isPresent(itemSearch.status()))
            predicate = predicate.and(qItem.status.eq(itemSearch.status()));

        if (isPresent(itemSearch.type()))
            predicate = predicate.and(qItem.type.eq(itemSearch.type()));

        return predicate;
    }
}
