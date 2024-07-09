package com.senior.assessment.domain.querydsl;

import com.senior.assessment.domain.entity.OrderItem;
import com.senior.assessment.domain.entity.QOrderItem;
import com.senior.assessment.domain.entity.QRelatorioItem;
import com.senior.assessment.domain.entity.RelatorioItem;
import org.springframework.stereotype.Repository;

@Repository
public class RelatorioItemQueryDslRepository extends GenericQueryDslRepository<RelatorioItem, QRelatorioItem> {
    @Override
    protected QRelatorioItem getEntityPath() {
        return QRelatorioItem.relatorioItem;
    }
}