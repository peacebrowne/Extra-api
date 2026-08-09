package com.example.extra.Entities;

import com.example.extra.DTO.UserDTO;
import com.example.extra.Enumerator.BudgetType;
import com.example.extra.Enumerator.PropertyType;
import com.example.extra.Enumerator.TaskStatus;
import com.example.extra.Enumerator.UrgencyType;
import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Represents a task posting or assignment in the system.
 */
@Data
public class  Task {

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
            message = "Client id must be a valid UUID")
    private String clientId;

    /**
     * Identifier of the provider assigned to the task (if any).
     * Typically maps to a User id representing the service provider.
     */
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "id must be a valid UUID")
    private String providerId;

    private UserDTO provider;

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
     * Defaults to FIXED pricing mode if not specified in the payload
     * */
    private BudgetType budgetType = BudgetType.FIXED;

    /**
     *  Ensures the client provides a financial amount greater than zero
     *  */
    @NotNull(message = "Budget amount is required.")
    @Min(value = 1, message = "Budget amount must be at least 1.")
    private Double budgetAmount;

    /**
     * Links the task to its specific sub-category type (e.g., TV Mounting)
     * */
    @NotBlank(message = "Sub-category ID is required.")
    private String subCategoryId;

    private List<String> images;

    @NotNull(message = "Please select when you need this task completed.")
    private UrgencyType urgencyType = UrgencyType.WITHIN_48_HOURS;

    private PropertyType propertyType = PropertyType.HOUSE;

    private Location location;

    /**
     * Creation timestamp for the task.
     */
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     *  If provided it must be now or in the future (can't schedule in the past).
     *  JSON format: ISO-8601 e.g. "2026-06-01T09:00:00Z"
     *  Validation: when present, must be @FutureOrPresent.
     *  */
    @FutureOrPresent(message = "scheduled date must be now or in the future")
    private LocalDateTime scheduledAt;

}