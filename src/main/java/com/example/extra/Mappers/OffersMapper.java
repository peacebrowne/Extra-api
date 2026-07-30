package com.example.extra.Mappers;

import com.example.extra.Entities.Offers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OffersMapper {
    @Insert("INSERT INTO offers (task_id, budget_type, budget_amount) VALUES (#{taskId}::UUID, #{budgetType}, #{budgetAmount})")
    void createTaskOffer(Offers offers);
}
