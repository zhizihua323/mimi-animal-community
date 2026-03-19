package org.mimi.corecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.mimi.community.entity.AnimalDailyComment;
import org.mimi.corecommunity.service.AnimalDailyCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/community/animalDailyComment")
public class AnimalDailyCommentController {

 @Autowired
 private AnimalDailyCommentService commentService;

 // 1. 接收前端发来的评论并保存
 @PostMapping
 public Map<String, Object> addComment(@RequestBody AnimalDailyComment comment) {
  boolean success = commentService.save(comment);

  // 模拟你们项目统一的返回格式
  Map<String, Object> result = new HashMap<>();
  result.put("code", success ? 200 : 500);
  result.put("message", success ? "操作成功" : "评论失败");
  return result;
 }

 // 2. 根据日常动态 ID 获取对应的所有评论，按时间倒序
 @GetMapping("/list/{dailyId}")
 public Map<String, Object> getComments(@PathVariable("dailyId") Long dailyId) {
  LambdaQueryWrapper<AnimalDailyComment> queryWrapper = new LambdaQueryWrapper<>();
  queryWrapper.eq(AnimalDailyComment::getDailyId, dailyId)
          .orderByDesc(AnimalDailyComment::getCreateTime);

  List<AnimalDailyComment> list = commentService.list(queryWrapper);

  Map<String, Object> result = new HashMap<>();
  result.put("code", 200);
  result.put("data", list);
  result.put("message", "操作成功");
  return result;
 }
}