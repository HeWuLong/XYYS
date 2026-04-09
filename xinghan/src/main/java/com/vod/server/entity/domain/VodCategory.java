package com.vod.server.entity.domain;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vod_category")
public class VodCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer sortWeight;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}