package com.example.coreshopping.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.coreshopping.service.AddressService;
import org.mimi.entity.User;
import org.mimi.result.Result;
import org.mimi.shopping.entity.Address;
import org.mimi.threadLocal.UserContextThreadLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shopping/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    // 1. 新增地址 (已补充默认地址排他逻辑)
    @PostMapping
    public Result createAddress(@RequestBody Address address){
        User user = UserContextThreadLocal.getUser();
        address.setUserId(user.getId());

        // 🌟 核心排他逻辑：如果当前新增的地址被设为默认(1)
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            // 先把该用户所有的地址设为非默认(0)
            LambdaUpdateWrapper<Address> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Address::getUserId, user.getId())
                    .set(Address::getIsDefault, 0);
            addressService.update(updateWrapper);
        }

        addressService.save(address);
        return Result.ok();
    }

    // 2. 修改地址 (🌟 新增的接口，供 address_edit.vue 调用)
    @PutMapping
    public Result updateAddress(@RequestBody Address address){
        User user = UserContextThreadLocal.getUser();
        // 安全起见，确保修改的是当前用户的地址
        address.setUserId(user.getId());

        // 🌟 核心排他逻辑：如果当前修改的地址被设为默认(1)
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            // 先把该用户所有的地址设为非默认(0)
            LambdaUpdateWrapper<Address> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Address::getUserId, user.getId())
                    .set(Address::getIsDefault, 0);
            addressService.update(updateWrapper);
        }

        // 然后根据 ID 更新这条地址的内容
        addressService.updateById(address);
        return Result.ok();
    }

    // 3. 获取地址列表 (优化：让默认地址排在最前面)
    @GetMapping
    public Result<List<Address>> pageAddressList(){
        User user = UserContextThreadLocal.getUser();

        LambdaQueryWrapper<Address> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(user.getId() != null, Address::getUserId, user.getId())
                .orderByDesc(Address::getIsDefault) // 🌟 优化：默认地址排在最上面
                .orderByDesc(Address::getId);       // 其他地址按添加时间倒序

        List<Address> list= addressService.list(lambdaQueryWrapper);
        return Result.ok(list);
    }

    // 4. 删除地址
    @PostMapping("/{id}")
    public Result deleteAddress(@PathVariable("id") Long id){
        addressService.removeById(id);
        return Result.ok();
    }
}