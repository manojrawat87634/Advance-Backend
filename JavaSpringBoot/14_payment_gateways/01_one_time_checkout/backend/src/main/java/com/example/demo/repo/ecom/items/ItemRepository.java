package com.example.demo.repo.ecom.items;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.models.ecom.items.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Fetch all non-deleted and active items (Uses idx_items_active_deleted)
    List<Item> findByIsDeletedFalseAndIsActiveTrue();

    // Paginated fetch for non-deleted, active items
    Page<Item> findByIsDeletedFalseAndIsActiveTrue(Pageable pageable);

    // Get a specific item by ID only if it is not soft-deleted
    Optional<Item> findByIdAndIsDeletedFalse(Long id);

    // Soft delete an item by setting is_deleted = true
    @Modifying
    @Query("UPDATE Item i SET i.isDeleted = true WHERE i.id = :id")
    int softDeleteById(@Param("id") Long id);
}