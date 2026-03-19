package org.mimi.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.mimi.config.WeChatProperties;
import org.mimi.entity.User;
import org.mimi.redis.utils.RedisConstant;
import org.mimi.result.Result;
import org.mimi.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequestMapping("/user/wxLogin")
@RestController
public class WxLoginController {

        @Autowired
        private WxMaService wxMaService;

        @Autowired
        private UserLoginService userLoginService;

        @Autowired
        private RedisTemplate<String, Serializable> redisTemplate;

        @GetMapping("wxLogin/{code}")
        public Result<Map> wxLogin(@PathVariable String code) throws Exception{

            WxMaJscode2SessionResult sessionResult =  wxMaService.getUserService().getSessionInfo(code);
            String openId = sessionResult.getOpenid();
            System.out.println(openId);
//            return openId;
            //从数据库中查询用户信息，如果

            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getOpenid,openId);
            User user = userLoginService.getOne(wrapper);
            if(user == null){
                User inituser = userLoginService.initUser(openId);
                userLoginService.save(inituser);

                user = inituser;
            }
            String uuid = UUID.randomUUID().toString().replace("-","");
            String userKey = RedisConstant.USER_LOGIN_KEY_PREFIX + uuid;
//            redisTemplate.setKeySerializer(new StringRedisSerializer());
//            GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
//            redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
            redisTemplate.opsForValue().set(userKey, user,RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.MINUTES);

            Map<String,Object> retMap = new HashMap<>();
            retMap.put("token",uuid);
            return  Result.ok(retMap);

        }

}
