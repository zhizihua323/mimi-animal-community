package org.mimi.clients.userclients;
import org.mimi.entity.User;
import org.mimi.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("userservice")
@Component
@RequestMapping("/user")
public interface UserClient {

    @GetMapping("/user/{id}")
    public User findById(@PathVariable("id") Long id);

    @GetMapping("/test")
    public Result<String> testUser(@RequestParam("name") String name);
}
