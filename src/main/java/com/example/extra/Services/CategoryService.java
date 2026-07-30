package com.example.extra.Services;

import com.example.extra.Entities.Category;
import com.example.extra.Entities.SubCategory;

import java.util.List;

public interface CategoryService {
    Category getCategoryByCategoryId(String categoryId);
    List<Category> getAllCategories();
    List<SubCategory> getSubCategoriesByCategoryId(String categoryId);
    List<Category> getSubCategoriesByCategoryName(String categoryName);
    List<Category> getSubCategoriesByCategoryNameAndCategoryId(String categoryName, String categoryId);
    List<Category> getSubCategoriesByCategoryIdAndCategoryName(String categoryId, String categoryName);
}
