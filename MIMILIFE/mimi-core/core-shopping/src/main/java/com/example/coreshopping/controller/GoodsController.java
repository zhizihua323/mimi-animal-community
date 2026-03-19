package com.example.coreshopping.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coreshopping.service.GoodsService;
import org.mimi.result.Result;
import org.mimi.shopping.entity.Goods;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
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


}
