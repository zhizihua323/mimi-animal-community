package org.mimi.controller;

import org.mimi.result.Result;
import org.mimi.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/user")
public class UserLogin {

    @Autowired
    private UserLoginService userLoginService;
    @GetMapping("/test")
    public Result<String> testUser(@RequestParam("name") String name){
        userLoginService.testRedis();
        return Result.ok("test",name);
    }

}
