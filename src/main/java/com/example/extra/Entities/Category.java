package com.example.extra.Entities;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Category {
    private String id;
    private String name;
    private LocalDateTime createdAt;

    // For sub-categories
    List<SubCategory> subCategories;
}
