package org.mimi.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("mimi_address")
public class Address implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(exist = false)
    private String stringId;

    private Long userId;

    private String name;

    private String tel;

    private String address;

    private Integer isDefault;

    public void setId(Long id) {
        this.id = id;
        // 同步stringId：id为null时stringId也为null
        this.stringId = id != null ? id.toString() : null;
    }
}
