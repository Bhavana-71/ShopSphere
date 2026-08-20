package com.shopsphere.shopsphere.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @NotNull
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stockQuantity;

    @NotNull
    private Long categoryId;
}