package com.example.extra.Entities;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubCategory {
        private String id;
        private String name;
        private LocalDateTime createdAt;
        private String description;
        private String categoryId;
}
