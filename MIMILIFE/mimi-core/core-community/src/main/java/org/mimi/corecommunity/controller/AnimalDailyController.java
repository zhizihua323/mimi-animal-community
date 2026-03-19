package org.mimi.corecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.mimi.community.entity.AnimalDaily;
import org.mimi.corecommunity.service.AnimalDailyService;
import org.mimi.entity.User;
import org.mimi.result.Result;
import org.mimi.threadLocal.UserContextThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/community/animalDaily")
public class AnimalDailyController {

    @Autowired
    private AnimalDailyService animalDailyService;

    @GetMapping("/{id}")
    public Result<List<AnimalDaily>> getAnimalDaily(@PathVariable Long id){
        LambdaQueryWrapper<AnimalDaily> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(id != null, AnimalDaily::getArchivalId,id);

        List<AnimalDaily> animalDailyList = animalDailyService.list(lambdaQueryWrapper);
        return Result.ok(animalDailyList);
    }

    @GetMapping("/detail/{id}")
    public Result<AnimalDaily> getAnimalDailyDetail(@PathVariable Long id){

        AnimalDaily animalDaily = animalDailyService.getById(id);
        return Result.ok(animalDaily);

    }


    @PostMapping
    public Result createAnimalDaily(@RequestBody AnimalDaily animalDaily){
        User user = UserContextThreadLocal.getUser();
        animalDaily.setCreateUser(user.getId());
        animalDaily.setCreateTime(LocalDateTime.now());
        animalDailyService.save(animalDaily);
        return  Result.ok();
    }

    @PostMapping("/delete")
    public Result deleteAnimalDaily(@RequestParam("id") Long id){
        animalDailyService.removeById(id);
        return Result.ok();
    }

}
