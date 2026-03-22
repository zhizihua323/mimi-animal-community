package org.mimi.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("animal_archival")
public class AnimalArchival implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField(exist = false)
    private String stringId;

    private String name;

    private String species;

    private Integer isWild;

    private LocalDate birthData;

    private String albumPhoto;

    private String description;

    private Integer isShow;

    private Long createUserId;

    private BigDecimal latitude;
    private BigDecimal longitude;

    public void setId(Long id) {
        this.id = id;
        // 同步stringId：id为null时stringId也为null
        this.stringId = id != null ? id.toString() : null;
    }
}
