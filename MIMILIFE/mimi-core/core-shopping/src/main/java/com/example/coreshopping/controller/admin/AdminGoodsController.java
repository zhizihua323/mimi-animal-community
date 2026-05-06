package com.example.coreshopping.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mimi.shopping.entity.Goods;
import com.example.coreshopping.service.GoodsService;
import org.mimi.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/admin/goods")
public class AdminGoodsController {

    @Autowired
    private GoodsService goodsService;

    // 1. 分页查询商品
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       String name) {
        Page<Goods> page = new Page<>(current, size);
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null && !name.isEmpty(), Goods::getGoodsName, name);
        return Result.ok(goodsService.page(page, wrapper));
    }

    // 2. 新增商品
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods) {
        return Result.ok(goodsService.save(goods));
    }

    // 3. 编辑商品 (包括修改库存、价格、上下架状态等)
    @PutMapping("/update")
    public Result update(@RequestBody Goods goods) {
        return Result.ok(goodsService.updateById(goods));
    }

    // 4. 删除商品
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.ok(goodsService.removeById(id));
    }
}