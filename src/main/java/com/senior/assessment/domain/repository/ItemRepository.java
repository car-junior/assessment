package com.senior.assessment.domain.repository;

import com.senior.assessment.domain.entity.Item;
import com.senior.assessment.domain.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {
    boolean existsItemById(UUID itemId);

    boolean existsItemByNameAndType(String name, ItemType type);

    boolean existsItemByNameAndTypeAndIdNot(String name, ItemType type, UUID id);
}
