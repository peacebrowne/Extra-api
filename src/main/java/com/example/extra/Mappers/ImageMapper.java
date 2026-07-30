package com.example.extra.Mappers;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ImageMapper {
    @Insert("INSERT INTO images (task_id, image_url) VALUES (#{taskId}::UUID, #{imageURL})")
    void insertTaskImage(@Param("taskId") String taskId, @Param("imageURL") String imageURL);

    @Delete("DELETE FROM images WHERE task_id = #{taskId}::UUID")
    void deleteTaskImage(@Param("taskId") String taskId);

    @Select("SELECT image_url FROM images WHERE user_id = #{id}::UUID")
    String getUserImage(@Param("id") String identifier);

    @Select("SELECT image_url FROM images WHERE task_id = #{taskId}::UUID")
    List<String> getTaskImages(@Param("taskId") String taskId);
}
