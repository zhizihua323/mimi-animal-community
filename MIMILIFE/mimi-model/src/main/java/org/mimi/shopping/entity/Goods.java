package org.mimi.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("mimi_goods")
public class Goods implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)  // 传给前端时自动转成 String，彻底消灭精度丢失！
    private Long id;

    // 新增：商品发布者ID (与数据库 user_id 对应)
    @JsonSerialize(using = ToStringSerializer.class)  // 必须加！防止用户ID传给前端时被抹零！
    private Long userId;

    private Long categoryId;

    private BigDecimal price;

    private String material;

    private String goodsName;

    private Double weight;

    private String picUrl;

    private Integer collect;

}