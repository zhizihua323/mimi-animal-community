package org.mimi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.mimi.entity.SysAdmin;
import org.mimi.mapper.SysAdminMapper;
import org.mimi.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@RestController
@RequestMapping("/admin/sys")
public class AdminLoginController {

    @Autowired
    private SysAdminMapper sysAdminMapper;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");

        // 1. 查库
        LambdaQueryWrapper<SysAdmin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysAdmin::getUsername, username);
        SysAdmin admin = sysAdminMapper.selectOne(queryWrapper);

        if (admin == null) {
            return Result.error("管理员账号不存在");
        }
        if (admin.getStatus() == 0) {
            return Result.error("该账号已被禁用");
        }

        // 2. 校验 MD5 密码
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!admin.getPassword().equals(md5Password)) {
            return Result.error("密码错误");
        }

        // 3. 生成 Token，并按照系统的老规矩存入 Redis (前缀为 admin:login:)
        String token = UUID.randomUUID().toString().replace("-", "");
        // 缓存 7 天
        redisTemplate.opsForValue().set("admin:login:" + token, admin, 7, TimeUnit.DAYS);

        // 4. 返回前端
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("token", token);
        resultData.put("name", admin.getName());
        resultData.put("avatar", admin.getAvatar());

        return Result.ok(resultData);
    }
}