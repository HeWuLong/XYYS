package com.vod.server.entity.domain;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;



@Data
@TableName("vod_history")
public class VodHistory {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long videoId;
    private String videoTitle;
    private String posterUrl;
    private Integer playProgress;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}