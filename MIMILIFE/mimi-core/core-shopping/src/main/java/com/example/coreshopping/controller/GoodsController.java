package com.example.coreshopping.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coreshopping.service.GoodsService;
import org.mimi.result.Result;
import org.mimi.shopping.entity.Goods;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shopping/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RBloomFilter<Long> bloomFilter;

    @GetMapping
    public Result<IPage<Goods>> pageGoodsList(){
        Page<Goods> goodsPage = new Page<>(1,10);
        IPage<Goods> goodsIPage = goodsService.page(goodsPage);

        return Result.ok(goodsIPage);
    }

    @PostConstruct
    public void init(){
        LambdaQueryWrapper<Goods> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(Goods::getId);
        List<Goods> goodsList = goodsService.list(lambdaQueryWrapper);

        for (Goods goods:goodsList){
            bloomFilter.add(goods.getId());
        }
    }

    /**
     * 用户发布二手商品接口
     */
    @PostMapping("/publish")
    public Result publish(@RequestBody Goods goods) {
        // 1. 获取当前登录用户 ID
        Long userId = 1961677606954274818L;

        goods.setUserId(userId);
        goods.setCollect(0);

        // 2. 自动给标题加注 "(二手)"
        if (goods.getGoodsName() != null && !goods.getGoodsName().contains("(二手)")) {
            goods.setGoodsName(goods.getGoodsName() + " (二手)");
        }

        // 3. 保存到数据库
        boolean saved = goodsService.save(goods);

        if (saved) {
            // 将新发布的商品 ID 加入布隆过滤器，防止缓存穿透误判
            if(goods.getId() != null) {
                bloomFilter.add(goods.getId());
            }
            // 🌟 修正：匹配 Result.ok(String message, T data)
            return Result.ok("发布成功", null);
        } else {
            // 🌟 修正：将 fail 改为 error，匹配你的 Result.error(String message)
            return Result.error("发布失败");
        }
    }

    /**
     * 1. 获取我发布的二手商品列表
     */
    @GetMapping("/myPublish")
    public Result getMyPublish() {
        Long userId = 1961677606954274818L;

        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        // 按 ID 倒序排列，保证新发布的在最上面显示
        queryWrapper.orderByDesc("id");

        List<Goods> list = goodsService.list(queryWrapper);

        // 这个不需要改，完美匹配 Result.ok(T data)
        return Result.ok(list);
    }

    /**
     * 2. 删除/下架我发布的商品
     */
    @DeleteMapping("/delete/{id}")
    public Result deletePublish(@PathVariable("id") Long id) {
        boolean removed = goodsService.removeById(id);

        if (removed) {
            // 修正
            return Result.ok("下架成功", null);
        } else {
            // 将 fail 改为 error
            return Result.error("下架失败");
        }
    }
}