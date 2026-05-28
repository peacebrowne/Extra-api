package com.example.extra.Mappers;


import com.example.extra.Entities.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {
    @Select("SELECT * FROM tasks WHERE id = #{id}::UUID")
    Task getTaskById(@Param("id") String id);

    @Select("INSERT INTO tasks " +
            "(client_id, title, description, status) " +
            "VALUES (#{clientId}::UUID, #{title}, #{description}, #{status}) RETURNING * ")
    Task createTask(Task task);

    @Update("UPDATE tasks " +
            "SET title = #{title}, description = #{description}, status = #{status} " +
            "WHERE id = #{id}::UUID")
    void updateTask(Task task);

    @Delete("DELETE FROM task WHERE id = #{id}::UUID")
    void deleteTask(@Param("id") String id);

    @Select("SELECT * FROM tasks WHERE client_id = (SELECT id FROM users WHERE email = #{email})::UUID")
    List<Task> getAllTasks(@Param("email") String email);

    @Update("UPDATE tasks SET status = 'ACCEPTED', provider_id = #{providerId}::UUID " +
            "WHERE id = #{id}::UUID " +
            "AND status = 'PENDING' " +
            "AND provider_id IS NULL " )
    void updateStatusToAccepted(@Param("id") String id, @Param("providerId") String providerId);

    @Update("UPDATE tasks SET status = 'PENDING_CONFIRMATION' " +
            "WHERE id = #{id}::UUID " +
            "AND provider_id = #{providerId}::UUID " +
            "AND status = 'IN_PROGRESS' ")
    void updateStatusToPendingConfirmation(@Param("id") String id, @Param("providerId") String providerId);
    
    @Update("UPDATE tasks SET status = 'COMPLETED' " +
            "WHERE id = #{id}::UUID " +
            "AND client_id = #{clientId}::UUID " +
            "AND status = 'PENDING_CONFIRMATION' ")
    void updateStatusToCompleted(@Param("id") String id, @Param("clientId") String clientId);

    @Update("UPDATE tasks SET status = 'IN_PROGRESS' " +
            "WHERE id = #{id}::UUID " +
            "AND provider_id = #{providerId}::UUID " +
            "AND status = 'ACCEPTED' ")
    void updateStatusToInProgress(String id, String providerId);

    @Update("UPDATE tasks SET status = 'CANCELLED', provider_id = NULL " +
            "WHERE id = #{id}::UUID " +
            "AND client_id = #{userId}::UUID " +
            "OR provider_id = #{userId}::UUID ")
    void updateStatusToCancelled(String id, String userId);

    @Update("UPDATE tasks SET status = 'DISPUTED' " +
            "WHERE id = #{id}::UUID " +
            "AND client_id = #{userId}::UUID " +
            "OR provider_id = #{userId}::UUID ")
    void updateStatusToDisputed(String id, String userId);

    @Update("UPDATE tasks SET status = 'PENDING' " +
            "WHERE id = #{id}::UUID " +
            "AND provider_id = #{userId}::UUID ")
    void updateStatusToPending(String id, String userId);




}
