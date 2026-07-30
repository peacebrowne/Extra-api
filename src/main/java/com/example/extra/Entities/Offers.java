package com.example.extra.Entities;

import com.example.extra.Enumerator.BudgetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Offers {
    private String id;

    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "id must be a valid UUID")
    private String taskId;

    /**
     * Identifier of the provider assigned to the task (if any).
     * Typically maps to a User id representing the service provider.
     */
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "id must be a valid UUID")
    private String providerId;

    /**
     * Defaults to FIXED pricing mode if not specified in the payload
     * */
    private BudgetType budgetType = BudgetType.FIXED;

    /**
     *  Ensures the client provides a financial amount greater than zero
     *  */
    @NotNull(message = "Budget amount is required.")
    @Min(value = 1, message = "Budget amount must be at least 1.")
    private Integer budgetAmount;


    /**
     * Creation timestamp for the task offer.
     */
    private LocalDateTime createdAt;


}
