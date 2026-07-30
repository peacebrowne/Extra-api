package com.example.extra.Controllers;

import com.example.extra.Services.Impl.CategoryServiceImpl;
import com.example.extra.Success.Success;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryServiceImpl categoryServiceImpl;

    @GetMapping
    public ResponseEntity<?> getAllCategories(){
        return Success.OK("Successfully retrieved all categories", categoryServiceImpl.getAllCategories());
    }

}
