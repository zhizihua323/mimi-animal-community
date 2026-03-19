package org.mimi.corecommunity.dto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mimi.community.entity.AnimalDailyComment;

@Mapper
public interface AnimalDailyCommentDto extends BaseMapper<AnimalDailyComment> {
}