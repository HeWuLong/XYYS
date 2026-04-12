package com.vod.server.entity.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vod_source")
public class VodSource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String apiUrl;
    private Integer parseType;
    private Integer status;
    private Integer sortWeight;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}