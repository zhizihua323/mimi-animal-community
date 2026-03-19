package org.mimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mimi.entity.User;

public interface UserLoginService extends IService<User> {
    public void testRedis();
    public User initUser(String openId);
}
