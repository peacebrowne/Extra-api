package com.example.extra.Mappers;

import com.example.extra.Entities.Location;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LocationMapper {
    @Select("INSERT INTO locations (task_id, address, latitude, longitude)  VALUES (#{taskId}::UUID, #{location.address}, #{location.latitude}, #{location.longitude}) RETURNING *")
    Location insertLocations(@Param("taskId") String taskId, @Param("location") Location location);

    @Update("UPDATE locations SET address = #{address}, latitude = #{latitude}, longitude = #{longitude} WHERE task_id = #{taskId}::UUID")
    void updateLocation(Location location);

    @Select("SELECT * FROM locations WHERE task_id = #{id}::UUID OR user_id = #{id}::UUID")
    Location getLocation(@Param("id") String id);


}
