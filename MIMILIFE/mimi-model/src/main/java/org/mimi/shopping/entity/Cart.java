package org.mimi.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mimi_cart")
public class Cart {

 @TableId(type = IdType.ASSIGN_ID)
 @JsonSerialize(using = ToStringSerializer.class)
 private Long id;

 @TableField(exist = false)
 private String stringId;

 private Long userId;        // 用户ID
 private Long goodsId;       // 商品ID
 private String goodsName;   // 商品名称
 private String picUrl;      // 商品图片
 private BigDecimal price;   // 加入时的单价
 private Integer num;        // 购买数量
 private Integer isChecked;  // 是否选中: 0-未选, 1-选中
 private LocalDateTime createTime;

 public void setId(Long id) {
  this.id = id;
  this.stringId = id != null ? id.toString() : null;
 }
}