package org.mimi.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("animal_daily")
public class AnimalDaily {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(exist = false)
    private String stringId;

    private String picUrl;

    private String text;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private LocalDateTime createTime;
    private Long createUser;
    private Long archivalId;

    public void setId(Long id) {
        this.id = id;
        // 同步stringId：id为null时stringId也为null
        this.stringId = id != null ? id.toString() : null;
    }
}
