package com.vod.server.entity.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vod_ad")
public class VodAd {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String picUrl;
    private String jumpLink;
    private String position;
    private Integer sort;
    private Integer status;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}