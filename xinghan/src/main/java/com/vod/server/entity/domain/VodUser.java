package com.vod.server.entity.domain;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vod_user")
public class VodUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private Integer status;          // 0-正常, 1-封禁
    private Integer isAdmin;         // 0-否, 1-是
    private String myInviteCode;
    private Long inviterId;
    private LocalDateTime vipExpireTime;
    private Integer coins;

    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}