package com.example.demo.controller.ecom.items;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ecom.item.ItemResponse;
import com.example.demo.models.ecom.items.Item;
import com.example.demo.repo.ecom.items.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    /**
     * Fetches all non-deleted and active products from the database.
     */
    public List<ItemResponse> getAllActiveItems() {
        return itemRepository.findByIsDeletedFalseAndIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Converts an Item Entity to an ItemResponse DTO.
     */
    private ItemResponse mapToResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .priceInPaise(item.getPrice())
                .priceInRupees(item.getPriceInRupees())
                .isActive(item.getIsActive())
                .isDeleted(item.getIsDeleted())
                .build();
    }
}