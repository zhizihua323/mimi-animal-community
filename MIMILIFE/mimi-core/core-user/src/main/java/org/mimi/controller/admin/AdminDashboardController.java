package org.mimi.controller.admin;

import org.mimi.mapper.UserMapper;
import org.mimi.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin // 必须加，解决浏览器跨域问题
@RestController
@RequestMapping("/admin/sys")
public class AdminDashboardController {

    @Autowired
    private UserMapper userMapper;

    // 答辩应急方案：如果你没有配置 Feign，我们直接用最简单的 RestTemplate 
    // 或者你可以先写死数据，保证演示时页面有东西。这里我先写一个聚合逻辑。
    private RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 1. 统计用户数 (本地 Mapper)
        data.put("userCount", userMapper.selectCount(null));

        // 2. 统计其他数据 (答辩演示演示方案：如果另外两个服务还没写好 count 接口，先给个合理的模拟数字)
        // 如果 core-community 启动在 8085，且你写了 count 接口，可以用下面这行：
        // data.put("dailyCount", restTemplate.getForObject("http://localhost:8085/admin/daily/count", Result.class).getData());
        
        data.put("dailyCount", 128); // 模拟数据
        data.put("goodsCount", 56);  // 模拟数据
        data.put("orderCount", 32);  // 模拟数据

        return Result.ok(data);
    }
}