package org.mimi.corecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mimi.community.entity.AnimalArchival;
import org.mimi.corecommunity.dto.AnimalArchivalDto;
import org.mimi.corecommunity.service.AnimalArchivalService;
import org.springframework.stereotype.Service;

@Service
public class AnimalArchivalServiceImpl extends ServiceImpl<AnimalArchivalDto, AnimalArchival> implements AnimalArchivalService  {
}
