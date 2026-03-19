package org.mimi.controller;

import org.mimi.clients.userclients.UserClient;
import org.mimi.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestFegin {
    @Autowired
    private UserClient userClient;

    @GetMapping("/test")
    public Result test(){
        Result<String> remoteResult = userClient.testUser("hello");
        // 根据 Result 的状态判断是否成功（需与 userservice 的 Result 结构一致）
        if (remoteResult.getCode().equals(200)) {
            System.out.println("远程调用成功，返回内容：" + remoteResult.getData());
        } else {
            System.out.println("远程调用失败，原因：" + remoteResult.getMessage());
        }
        return Result.ok();
    }
}
