package org.mimi.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mimi_order")
public class Order implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(exist = false)
    private String stringId;

    private Long userId;

    private String userOpenid;

    private String userAddress;

    private Integer status;

    private LocalDateTime orderTime;

    private LocalDateTime payTime;

    private LocalDateTime deliverTime;

    private LocalDateTime receivedTime;

    private String goodsName;

    private BigDecimal price;

    private Integer num;

    private BigDecimal freight;

    private String tel;

    private Long goodsId;

    private String goodsUrl;

    private Integer isExpired;

    public void setId(Long id) {
        this.id = id;
        // 同步stringId：id为null时stringId也为null
        this.stringId = id != null ? id.toString() : null;
    }




}
