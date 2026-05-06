package org.mimi.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mimi.entity.User;
// 由于 Admin 端通常只需简单查改，直接使用 Mapper 最稳妥
import org.mimi.mapper.UserMapper;
import org.mimi.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@CrossOrigin
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Resource
    private UserMapper userMapper;

    // 1. 分页查询用户 (支持按名字模糊搜索)
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       String name) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null && !name.isEmpty(), User::getName, name);
        return Result.ok(userMapper.selectPage(page, wrapper));
    }

    // 2. 修改用户信息 (核心用于修改 status 字段来封禁/解封用户)
    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        return Result.ok(userMapper.updateById(user));
    }
}