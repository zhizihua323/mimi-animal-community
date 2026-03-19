package com.example.coreshopping.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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


    @PostMapping
    public Result createAddress(@RequestBody Address address){
        User user = UserContextThreadLocal.getUser();
        address.setUserId(user.getId());
        addressService.save(address);
        return Result.ok();
    }

    @GetMapping
    public Result<List<Address>> pageAddressList(){
        User user = UserContextThreadLocal.getUser();

        LambdaQueryWrapper<Address> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(user.getId() != null, Address::getUserId,user.getId());

        List<Address> list= addressService.list(lambdaQueryWrapper);
        return Result.ok(list);
    }


    @PostMapping("/{id}")
    public Result deleteAddress(@PathVariable("id") Long id){
        addressService.removeById(id);
        return Result.ok();
    }


}
