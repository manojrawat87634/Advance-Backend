package com.example.demo.controller.ecom.items;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ecom.item.ItemResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    // GET /api/v1/items - Get all active products
    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllProducts() {
        List<ItemResponse> items = itemService.getAllActiveItems();
        return ResponseEntity.ok(items);
    }
}