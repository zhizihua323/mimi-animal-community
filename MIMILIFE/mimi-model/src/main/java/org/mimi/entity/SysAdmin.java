package org.mimi.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_admin")
public class SysAdmin implements Serializable {
    @TableId
    private Long id;
    private String username;
    private String password;
    private String name;
    private String avatar;
    private Integer status;
    private Date createTime;
}