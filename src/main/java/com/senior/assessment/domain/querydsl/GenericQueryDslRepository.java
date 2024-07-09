package com.senior.assessment.domain.querydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.senior.assessment.domain.entity.QItem;
import com.senior.assessment.domain.enums.ItemStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public abstract class GenericQueryDslRepository<T, E extends EntityPath<T>> {
    @PersistenceContext
    private EntityManager entityManager;

    protected abstract E getEntityPath();

    @Bean
    private JPAQueryFactory queryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    public List<T> findAll() {
        var predicate = new AtomicReference<Predicate>();
//        var session = entityManager.unwrap(Session.class);
//        session.enableFilter("statusActive").setParameter("status", "ACTIVE");
////        JPAQuery<T> query = ;
////        query.from(getEntityPath());
        var query = queryFactory().selectFrom(getEntityPath());
        // Qsku.class
        var item = Arrays.stream(getEntityPath().getClass().getDeclaredFields()).filter(f -> f.getType().equals(QItem.class)).findFirst();
        item.ifPresent(field -> {
            var it = QItem.item;
            field.setAccessible(true);
            try {
                // Acessa a instância do campo qItem(vulgo SKU) dentro da getEntityPath(), assim sendo possivel fazer referencia ao
                // campo que quero fazer o join
                var qItem = (QItem) field.get(getEntityPath());
                predicate.set(it.status.eq(ItemStatus.DISABLED));
                query.innerJoin(qItem, it);
//                query.where(it.status.eq(ItemStatus.ACTIVE));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
//            query.innerJoin(getEntityPath()+ "." + item.get().getName(), qItem)
//                    .where(qItem.status.eq(ItemStatus.ACTIVE));
//            query.innerJoin(getEntityPath().getMetadata())
        });
        return query.where(predicate.get()).fetch();
    }
}
