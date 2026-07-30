package com.example.extra.Entities;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCategory {
    private String id;
    private String categoryId;
    private String taskId;
    private LocalDateTime createdAt;
}
