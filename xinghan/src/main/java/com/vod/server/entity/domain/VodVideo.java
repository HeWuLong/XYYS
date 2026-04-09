package com.vod.server.entity.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vod_video")
public class VodVideo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String title;
    private String posterUrl;
    private String region;
    private Integer publishYear;
    private String sourceId;
    private String playFormat;
    private String playUrl;
    private String extInfo;      // JSON 字符串，实际可用 MySQL JSON 类型
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}