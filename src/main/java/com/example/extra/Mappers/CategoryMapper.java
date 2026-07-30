package com.example.extra.Mappers;

import com.example.extra.Entities.Category;
import com.example.extra.Entities.SubCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT * FROM categories WHERE id = #{id}::UUID")
    Category getCategoryById(@Param("id") String id);

    @Select("SELECT * FROM sub_categories WHERE id = #{id}:UUID")
    Category getSubCategoryById(@Param("id") String id);

    @Select("SELECT * FROM sub_categories WHERE category_id = #{categoryId}::UUID")
    Category getSubCategoryByCategoryId(@Param("categoryId") String categoryId);

    @Select("SELECT * FROM sub_categories sc JOIN categories c ON sc.category_id = c.id WHERE sc.category_id = #{categoryId}::UUID")
    List<SubCategory> getSubCategoriesByCategoryId(@Param("categoryId") String categoryId);

    @Select("SELECT * FROM categories")
    List<Category> getAllCategories();
}
