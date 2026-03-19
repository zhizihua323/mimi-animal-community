package org.mimi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mimi.dto.UserLoginDto;
import org.mimi.entity.User;
import org.mimi.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class UserLoginServiceImpl extends ServiceImpl<UserLoginDto, User> implements UserLoginService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void testRedis(){
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("first", "value");
    }

    public User initUser(String openId){
        User user = new User();
        user.setOpenid(openId);
        user.setName("喵喵星人");
        user.setStatus(1);

        return user;
    }

}
