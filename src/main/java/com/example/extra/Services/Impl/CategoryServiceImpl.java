package com.example.extra.Services.Impl;

import com.example.extra.Entities.Category;
import com.example.extra.Entities.SubCategory;
import com.example.extra.Exceptions.Custom.BadRequest;
import com.example.extra.Exceptions.Custom.InternalServerError;
import com.example.extra.Mappers.CategoryMapper;
import com.example.extra.Services.CategoryService;
import com.example.extra.Utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    @Cacheable(value = "category", key = "#id")
    public Category getCategoryByCategoryId(String id) {
        try {
            if (!ValidationUtils.validateUUID(id)) throw new BadRequest("Category Id is not valid");
            return  categoryMapper.getCategoryById(id);
        } catch (BadRequest e) {
            log.error("Get Category By Category ID Validation Error: {}", e.getMessage());
            throw e;
        }catch (Exception e) {
            log.error(e.getMessage());
            throw new InternalServerError("Unexpected Error occurred while getting the category by category Id", e);
        }
    }

    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> getAllCategories() {
        List<Category> categories = categoryMapper.getAllCategories();
        categories.forEach(category -> {
            List<SubCategory> subCategory = categoryMapper.getSubCategoriesByCategoryId(category.getId());
            category.setSubCategories(subCategory);
        });
        return categories;
    }

    @Override
    @Cacheable(value = "subCategories", key = "#id")
    public List<SubCategory> getSubCategoriesByCategoryId(String id) {
        try {
            if (!ValidationUtils.validateUUID(id)) throw new BadRequest("Category Id is not valid");
            return categoryMapper.getSubCategoriesByCategoryId(id);
        }catch (BadRequest e) {
            log.error("Get Sub-Categories By Category ID Validation Error: {}", e.getMessage());
            throw e;
        }catch (Exception e) {
            log.error(e.getMessage());
            throw new InternalServerError("Unexpected Error occurred while getting the category by category Id", e);
        }
    }

    @Override
    public List<Category> getSubCategoriesByCategoryName(String name) {
        return List.of();
    }

    @Override
    public List<Category> getSubCategoriesByCategoryNameAndCategoryId(String name, String id) {
        return List.of();
    }

    @Override
    public List<Category> getSubCategoriesByCategoryIdAndCategoryName(String id, String name) {
        return List.of();
    }
}
