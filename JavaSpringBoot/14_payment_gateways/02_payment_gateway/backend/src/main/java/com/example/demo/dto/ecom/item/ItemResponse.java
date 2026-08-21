package com.example.demo.dto.ecom.item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private String name;
    private String description;
    private Long priceInPaise;
    private Double priceInRupees;
    private Boolean isActive;
    private Boolean isDeleted;
}