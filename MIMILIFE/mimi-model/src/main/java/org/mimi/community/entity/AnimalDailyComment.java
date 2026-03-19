package org.mimi.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("animal_daily_comment")
public class AnimalDailyComment {
 @TableId(type = IdType.ASSIGN_ID)
 private Long id;

 @TableField(exist = false)
 private String stringId;

 private Long dailyId;
 private Long userId;
 private String content;
 private LocalDateTime createTime;

 public void setId(Long id) {
  this.id = id;
  this.stringId = id != null ? id.toString() : null;
 }
}