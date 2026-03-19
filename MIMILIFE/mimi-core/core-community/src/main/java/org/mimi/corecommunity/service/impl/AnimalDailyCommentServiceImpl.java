package org.mimi.corecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mimi.community.entity.AnimalDailyComment;
import org.mimi.corecommunity.dto.AnimalDailyCommentDto;
import org.mimi.corecommunity.service.AnimalDailyCommentService;
import org.springframework.stereotype.Service;

@Service
public class AnimalDailyCommentServiceImpl extends ServiceImpl<AnimalDailyCommentDto, AnimalDailyComment> implements AnimalDailyCommentService {
}