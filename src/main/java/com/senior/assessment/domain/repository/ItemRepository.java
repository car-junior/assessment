package com.senior.assessment.domain.repository;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID>, QuerydslPredicateExecutor<Item> {
    boolean existsItemById(UUID itemId);

    boolean existsItemByNameAndType(String name, ItemType type);

    boolean existsItemByNameAndTypeAndIdNot(String name, ItemType type, UUID id);

    Set<Item> getAllByIdIn(Set<UUID> ids);
}
