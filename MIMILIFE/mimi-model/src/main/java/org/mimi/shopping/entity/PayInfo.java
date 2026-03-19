package org.mimi.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("mimi_pay_info")
public class PayInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String material;

    private Integer payPlatform;

    private String imageUrl;

    private BigDecimal price;

    private Integer num;

    private BigDecimal totalPrice;


}
