package org.mimi.dto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mimi.entity.User;

@Mapper
public interface UserLoginDto extends BaseMapper<User> {
}
