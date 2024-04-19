package com.senior.assessment.domain.querydsl;

import com.querydsl.core.types.Predicate;
import com.senior.assessment.domain.entity.QItem;
import com.senior.assessment.domain.querydsl.search.ItemSearch;

import static com.senior.assessment.utilities.Utils.*;

public class ItemDslPredicate {

    private ItemDslPredicate() {}

    public static Predicate expression(ItemSearch itemSearch) {
        var qItem = QItem.item;
        var predicate = qItem.id.isNotNull();

        if (isPresent(itemSearch.query())) {
            var query = "%" + unaccented(itemSearch.query()) + "%";
            predicate = predicate.and(unaccentedExpression(qItem.name).likeIgnoreCase(query));
        }

        if (isPresent(itemSearch.id()))
            predicate = predicate.and(qItem.id.eq(itemSearch.id()));

        if (isPresent(itemSearch.status()))
            predicate = predicate.and(qItem.status.eq(itemSearch.status()));

        if (isPresent(itemSearch.type()))
            predicate = predicate.and(qItem.type.eq(itemSearch.type()));

        return predicate;
    }
}
