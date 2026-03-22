package org.mimi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.mimi.entity.User;
import org.mimi.result.Result;
import org.mimi.entity.Collect;
import org.mimi.service.CollectService;
import org.mimi.threadLocal.UserContextThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/user/collect")
public class CollectController {

 @Autowired
 private CollectService collectService;

 // 1. 收藏 / 取消收藏 (智能切换)
 @PostMapping("/toggle")
 public Result toggleCollect(@RequestBody Collect collect) {
  User user = UserContextThreadLocal.getUser();

  // 检查数据库是否已有该收藏
  LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
  wrapper.eq(Collect::getUserId, user.getId())
          .eq(Collect::getTargetId, collect.getTargetId())
          .eq(Collect::getTargetType, collect.getTargetType());

  Collect exist = collectService.getOne(wrapper);

  if (exist != null) {
   // 已存在 -> 取消收藏
   collectService.removeById(exist.getId());
   return Result.ok("已取消收藏");
  } else {
   // 不存在 -> 添加收藏
   collect.setUserId(user.getId());
   collect.setCreateTime(LocalDateTime.now());
   collectService.save(collect);
   return Result.ok("收藏成功");
  }
 }

 // 2. 获取我的收藏列表 (按时间倒序)
 @GetMapping("/list")
 public Result<List<Collect>> getCollectList() {
  User user = UserContextThreadLocal.getUser();

  LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
  wrapper.eq(Collect::getUserId, user.getId())
          .orderByDesc(Collect::getCreateTime);

  return Result.ok(collectService.list(wrapper));
 }
}