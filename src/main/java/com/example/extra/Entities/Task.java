package com.example.extra.Entities;

import com.example.extra.Enumerator.TaskStatus;
import jakarta.validation.constraints.*;

import lombok.Data;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;


/**
 * Represents a task posting or assignment in the system.
 */
@Data
public class Task {

    /**
     * Unique identifier for the task (for example a UUID or database id).
     * Example: "123e4567-e89b-12d3-a456-426614174000"
     */
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "id must be a valid UUID")
    private String id;

    /**
     * Identifier of the client who created/requested the task.
     * Typically maps to a User id in the system (e.g. 'client-uuid').
     */
    @NotBlank(message = "clientId is required")
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "id must be a valid UUID")
    private String clientId;

    /**
     * Identifier of the provider assigned to the task (if any).
     * Typically maps to a User id representing the service provider.
     */
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "id must be a valid UUID")
    private String providerId;

    /**
     * Short, human-readable title for the task.
     * Example: "Install shelving" or "House cleaning - 3 hours".
     */
    @NotBlank(message = "title is required")
    @Size(max = 100, message = "title must be at most 100 characters")
    private String title;

    /**
     * Detailed description of the task, requirements, location, etc.
     * Can contain multiple sentences and any relevant instructions.
     */
    @NotBlank(message = "description is required")
    @Size(max = 2000, message = "description must be at most 2000 characters")
    private String description;

    /**
     * Current task status. Allowed values:
     * "PENDING", "ACCEPTED", "COMPLETED", "CANCELLED".
     */
    private TaskStatus status = TaskStatus.PENDING;

    /**
     * Creation timestamp for the task.
     */
    private LocalDateTime created;

    /**
     *  If provided it must be now or in the future (can't schedule in the past).
     *  JSON format: ISO-8601 e.g. "2026-06-01T09:00:00Z"
     *  Validation: when present, must be @FutureOrPresent.
     *  */
    @FutureOrPresent(message = "scheduled date must be now or in the future")
    private LocalDateTime scheduled;
}