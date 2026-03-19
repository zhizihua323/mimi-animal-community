package org.mimi.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
@Data
@TableName("mimi_category_family")
public class CategoryFamily {

    @TableId(type =  IdType.ASSIGN_ID)
    private Long id;

    @TableField(exist = false)
    private String stringId;

    private String category;

}
