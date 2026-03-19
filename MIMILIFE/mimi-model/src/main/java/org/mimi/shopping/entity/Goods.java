package org.mimi.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("mimi_goods")
public class Goods {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(exist = false)
    private String stringId;

    private Long categoryId;

    private BigDecimal price;

    private String material;

    private String goodsName;

    private Double weight;

    private String picUrl;

    private Integer collect;

}
