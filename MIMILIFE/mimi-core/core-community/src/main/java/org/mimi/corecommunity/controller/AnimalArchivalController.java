package org.mimi.corecommunity.controller;


import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mimi.community.entity.AnimalArchival;
import org.mimi.corecommunity.service.AnimalArchivalService;
import org.mimi.entity.User;
import org.mimi.redis.config.BloomArchivalConfig;
import org.mimi.redis.utils.RedisConstant;
import org.mimi.result.Result;
import org.mimi.shopping.entity.Goods;
import org.mimi.threadLocal.UserContextThreadLocal;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;

@RestController
@RequestMapping("/community/animalArchival")
public class AnimalArchivalController {

    @Autowired
    private AnimalArchivalService animalArchivalService;

    @Autowired
    private RedisTemplate<String,Serializable> redisTemplate;

    @Autowired
    private RBloomFilter<Long> bloomFilter;

    @PostConstruct
    public void init(){
        LambdaQueryWrapper<AnimalArchival> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(AnimalArchival::getId);
        List<AnimalArchival> archivalList = animalArchivalService.list(lambdaQueryWrapper);

        for (AnimalArchival archival:archivalList){
            bloomFilter.add(archival.getId());
        }
    }


    @PostMapping
    public Result createAnimalArchival(@RequestBody AnimalArchival animalArchival){
        User user = UserContextThreadLocal.getUser();
        redisTemplate.opsForValue().set("key",user);
        animalArchival.setCreateUserId(user.getId());
        animalArchivalService.save(animalArchival);

        return Result.ok();
    }

    @PostMapping("/updated")
    public Result updateAnimalArchival(@RequestBody AnimalArchival animalArchival){

        boolean isContain = bloomFilter.contains(Long.valueOf(animalArchival.getStringId()));
        if(!isContain){
            Result.error("非法id");
        }
        User user = UserContextThreadLocal.getUser();
        animalArchival.setCreateUserId(user.getId());
        animalArchival.setId(Long.parseLong(animalArchival.getStringId()));

        animalArchivalService.updateById(animalArchival);

        return Result.ok();
    }

    @GetMapping("/getOne")
    public Result queryAnimalArchivalById(@RequestParam("id") Long id){
        String cacheKey = RedisConstant.ANIMAL_ARCHIVAL_KEY_PREFIX + id;
        AnimalArchival animalArchivalRedis = (AnimalArchival) redisTemplate.opsForValue().get(cacheKey);
        if(animalArchivalRedis == null){
            AnimalArchival animalArchivalDB = animalArchivalService.getById(id);
            redisTemplate.opsForValue().set(cacheKey,animalArchivalDB);
            return Result.ok(animalArchivalDB);
        }

        return Result.ok(animalArchivalRedis);
    }

    @GetMapping
    public Result<IPage<AnimalArchival>> listAnimalArchival(){
        LambdaQueryWrapper<AnimalArchival> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        User user = UserContextThreadLocal.getUser();

        Page<AnimalArchival> page = new Page<>(1,10);

        lambdaQueryWrapper.eq(user.getId() != null, AnimalArchival::getCreateUserId,user.getId());
        IPage<AnimalArchival> animalArchivalPage = animalArchivalService.page(page,lambdaQueryWrapper);

        return Result.ok(animalArchivalPage);
    }


    @PostMapping("/{id}")
    public Result deleteAnimalArchivalById(@PathVariable("id") Long id){
        boolean isDeleted = animalArchivalService.removeById(id);
        if (isDeleted) {
            return Result.ok("动物档案删除成功",null);
        } else {
            return Result.error("未找到ID为" + id + "的动物档案");
        }
    }

    /**
     * 获取带有经纬度的动物档案列表（给前端地图定位使用）
     */
    @GetMapping("/mapList")
    public Result listMapAnimalArchival() {
        LambdaQueryWrapper<AnimalArchival> queryWrapper = new LambdaQueryWrapper<>();
        // 核心条件：只查出经度、纬度都不为空的数据，防止前端地图组件报错
        queryWrapper.isNotNull(AnimalArchival::getLatitude)
                .isNotNull(AnimalArchival::getLongitude);
        // 数据库里有 is_show 字段，只展示公开的
         queryWrapper.eq(AnimalArchival::getIsShow, 1);

        // 查询出列表
        List<AnimalArchival> list = animalArchivalService.list(queryWrapper);

        // 直接用你封装好的 Result 返回
        return Result.ok(list);
    }
}
