package org.mimi.corecommunity.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mimi.community.entity.AnimalDaily;
import org.mimi.corecommunity.service.AnimalDailyService;
import org.mimi.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/admin/daily")
public class AdminDailyController {

    @Autowired
    private AnimalDailyService animalDailyService;

    // 1. 分页查询所有动态 (按发布时间倒序排列)
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size) {
        Page<AnimalDaily> page = new Page<>(current, size);
        LambdaQueryWrapper<AnimalDaily> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AnimalDaily::getCreateTime);
        return Result.ok(animalDailyService.page(page, wrapper));
    }

    // 2. 删除违规动态
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.ok(animalDailyService.removeById(id));
    }
}