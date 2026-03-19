package com.example.coreshopping.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coreshopping.service.CategoryFamilyService;
import org.mimi.result.Result;
import org.mimi.shopping.entity.CategoryFamily;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shopping/categoryFamily")
public class CategoryFamilyController {

    @Autowired
    private CategoryFamilyService categoryFamilyService;

    @GetMapping
    public Result<IPage<CategoryFamily>> pageCategoryList(){
        Page<CategoryFamily> page = new Page<>(1,15);
        IPage<CategoryFamily> categoryFamilyIPage = categoryFamilyService.page(page);

        return Result.ok(categoryFamilyIPage);
    }

}
