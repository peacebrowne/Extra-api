package com.example.extra.Mappers;


import com.example.extra.Entities.Task;
import com.example.extra.Enumerator.TaskStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {
    @Select("SELECT * FROM tasks WHERE id = #{id}::UUID")
    Task getTaskById(@Param("id") String id);

    @Select("INSERT INTO tasks " +
            "(client_id, title, description, status, budget_amount, budget_type," +
            "scheduled_at, sub_category_id, urgency_type, property_type) " +
            "VALUES (#{clientId}::UUID, #{title}, #{description}, #{status}, #{budgetAmount}, #{budgetType}, " +
            "#{scheduledAt}, #{subCategoryId}::UUID, #{urgencyType}, #{propertyType}) RETURNING * ")
    Task createTask(Task task);

    @Select("UPDATE tasks " +
            "SET title = #{title}, description = #{description}, urgency_type = #{urgencyType}, " +
            "budget_type = #{budgetType}, budget_amount = #{budgetAmount}, " +
            "sub_category_id = #{subCategoryId}::UUID, " +
            "property_type = #{propertyType}, scheduled_at = #{scheduledAt} " +
            "WHERE id = #{id}::UUID " +
            "RETURNING *")
    Task updateTask(Task task);

    @Delete("DELETE FROM tasks WHERE id = #{id}::UUID")
    void deleteTask(@Param("id") String id);

    @Select("SELECT * FROM tasks WHERE client_id = #{clientId}::UUID ORDER BY created_at DESC")
    List<Task> getAllTasks(@Param("clientId") String clientId);

    @Select("SELECT * FROM tasks " +
            "WHERE status = 'IN_PROGRESS' " +
            "AND (client_id = #{userId}::UUID OR provider_id = #{userId}::UUID) " +
            "ORDER BY created_at DESC")
    List<Task> getInProgressTasks(@Param("userId") String userId);

    @Select("UPDATE tasks SET status = 'ACCEPTED', provider_id = #{providerId}::UUID " +
            "WHERE id = #{id}::UUID " +
            "AND status = 'PENDING' " +
            "AND provider_id IS NULL " +
            "RETURNING *" )
    Task updateStatusToAccepted(@Param("id") String id, @Param("providerId") String providerId);

    @Select("UPDATE tasks SET status = 'PENDING_CONFIRMATION' " +
            "WHERE id = #{id}::UUID " +
            "AND provider_id = #{providerId}::UUID " +
            "AND status = 'IN_PROGRESS' " +
            "RETURNING *")
    Task updateStatusToPendingConfirmation(@Param("id") String id, @Param("providerId") String providerId);
    
    @Update("UPDATE tasks SET status = 'COMPLETED' " +
            "WHERE id = #{id}::UUID " +
            "AND client_id = #{clientId}::UUID " +
            "AND status = 'PENDING_CONFIRMATION' " +
            "RETURNING *")
    Task updateStatusToCompleted(@Param("id") String id, @Param("clientId") String clientId);

    @Select("UPDATE tasks SET status = 'IN_PROGRESS' " +
            "WHERE id = #{id}::UUID " +
            "AND provider_id = #{providerId}::UUID " +
            "AND status = 'ACCEPTED' " +
            "RETURNING *")
    Task updateStatusToInProgress(String id, String providerId);

    @Select("UPDATE tasks SET status = 'CANCELLED', provider_id = NULL " +
            "WHERE id = #{id}::UUID " +
            "AND client_id = #{userId}::UUID " +
            "OR provider_id = #{userId}::UUID " +
            "RETURNING *")
    Task updateStatusToCancelled(String id, String userId);

    @Select("UPDATE tasks SET status = 'DISPUTED' " +
            "WHERE id = #{id}::UUID " +
            "AND client_id = #{userId}::UUID " +
            "OR provider_id = #{userId}::UUID ")
    Task updateStatusToDisputed(String id, String userId);

    @Select("UPDATE tasks SET status = 'PENDING', provider_id = NULL " +
            "WHERE id = #{id}::UUID " +
            "AND provider_id = #{userId}::UUID " +
            "RETURNING *")
    Task updateStatusToPending(String id, String userId);

    List<Task> getUserSearchTask(
            @Param("clientId") String clientId,
            @Param("term") String term,
            @Param("status") String status
    );
}
