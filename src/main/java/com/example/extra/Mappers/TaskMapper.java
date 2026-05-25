package com.example.extra.Mappers;


import com.example.extra.Entities.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {
    @Select("SELECT * FROM tasks WHERE id = #{id}::UUID")
    Task getTaskById(@Param("id") String id);

    @Select("INSERT INTO tasks " +
            "(clientId, title, description, status) " +
            "VALUES (#{clientId}::UUID, #{title}, #{description}, #{status}) RETURNING * ")
    Task createTask(Task task);

    @Update("UPDATE tasks " +
            "SET title = #{title}, description = #{description}, status = #{status}, scheduled = #{scheduled} " +
            "WHERE id = #{id}::UUID")
    void updateTask(Task task);

    @Delete("DELETE FROM task WHERE id = #{id}::UUID")
    void deleteTask(@Param("id") String id);

    @Select("SELECT * FROM tasks WHERE clientId = (SELECT id FROM users WHERE email = #{email})::UUID")
    List<Task> getAllTasks(@Param("email") String email);

    @Select("UPDATE tasks SET status = 'ACCEPTED', providerId = #{providerId}::UUID " +
            "WHERE id = #{id}::UUID " +
            "AND status = 'PENDING' " +
            "AND providerId IS NULL " +
            "RETURNING *")
    Task updateStatusToAccepted(@Param("id") String id, @Param("providerId") String providerId);

    @Select("UPDATE tasks SET status = 'PENDING_CONFIRMATION' " +
            "WHERE id = #{id}::UUID " +
            "AND providerId = #{providerId}::UUID " +
            "AND status = 'IN_PROGRESS' " +
            "RETURNING *")
    Task updateStatusToPendingConfirmation(@Param("id") String id, @Param("providerId") String providerId);
    
    @Select("UPDATE tasks SET status = 'COMPLETED' " +
            "WHERE id = #{id}::UUID " +
            "AND clientId = #{clientId}::UUID " +
            "AND status = 'PENDING_CONFIRMATION' " +
            "RETURNING *")
    Task updateStatusToCompleted(@Param("id") String id, @Param("clientId") String clientId);

    @Select("UPDATE tasks SET status = 'IN_PROGRESS' " +
            "WHERE id = #{id}::UUID " +
            "AND providerId = #{providerId}::UUID " +
            "AND status = 'ACCEPTED' " +
            "RETURNING *")
    Task updateStatusToInProgress(String id, String providerId);
}
