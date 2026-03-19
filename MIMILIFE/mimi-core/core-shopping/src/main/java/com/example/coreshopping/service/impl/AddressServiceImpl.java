package com.example.coreshopping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreshopping.dto.AddressDto;
import com.example.coreshopping.service.AddressService;
import org.mimi.shopping.entity.Address;
import org.springframework.stereotype.Service;


@Service
public class AddressServiceImpl extends ServiceImpl<AddressDto, Address> implements AddressService {

}
