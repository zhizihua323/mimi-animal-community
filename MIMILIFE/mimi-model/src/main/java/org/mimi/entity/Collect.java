package org.mimi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("mimi_collect")
public class Collect {

 @TableId(type = IdType.ASSIGN_ID)
 @JsonSerialize(using = ToStringSerializer.class)
 private Long id;
 @TableField(exist = false)
 private String stringId;

 private Long userId;
 @JsonSerialize(using = ToStringSerializer.class)
 private Long targetId;      // 被收藏的业务ID
 private Integer targetType; // 1-商品, 2-日常, 3-定位

 private String title;       // 冗余：标题
 private String picUrl;      // 冗余：图片
 private String summary;     // 冗余：摘要说明

 private LocalDateTime createTime;

 public void setId(Long id) {
  this.id = id;
  this.stringId = id != null ? id.toString() : null;
 }
}