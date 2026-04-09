package com.vod.server.entity.vo;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private Integer status;
    private Integer isAdmin;
    private String myInviteCode;
    private Long inviterId;
    private LocalDateTime vipExpireTime;
    private Integer coins;
    private String shareUrl;   // 分享链接
}