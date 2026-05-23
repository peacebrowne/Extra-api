package com.example.extra.Mappers;


import com.example.extra.Entities.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {
    @Select("SELECT * FROM tasks WHERE id = #{id}::UUID")
    Task getTaskById(@Param("id") String id);

    @Select("INSERT INTO tasks (clientId, title, description, status) VALUES (#{clientId}::UUID, #{title}, #{description}, #{status}) RETURNING * ")
    Task createTask(Task task);

    @Update("UPDATE tasks SET title = #{title}, description = #{description}, status = #{status}, scheduled = #{scheduled} WHERE id = #{id}::UUID")
    void updateTask(Task task);

    @Delete("DELETE FROM task WHERE id = #{id}::UUID")
    void deleteTask(@Param("id") String id);

    @Select("SELECT * FROM tasks WHERE clientId = (SELECT id FROM users WHERE email = #{email})::UUID")
    List<Task> getAllTasks(@Param("email") String email);

    @Select("UPDATE tasks SET status = 'ACCEPTED', providerId = #{providerId}::UUID WHERE id = #{id}::UUID AND status = 'PENDING' RETURNING *")
    Task updateTaskStatus(@Param("id") String id, @Param("providerId") String providerId);
}
