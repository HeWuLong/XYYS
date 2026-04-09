package com.vod.server.utils;
import com.vod.server.entity.domain.VodUser;
import com.vod.server.entity.vo.UserInfoVO;

public class UserInfoConverter {
    private static final String SHARE_BASE_URL = "https://你的域名/#/pages/register/register?code=";
    public static UserInfoVO toVO(VodUser user) {
        if (user == null) return null;
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setStatus(user.getStatus());
        vo.setIsAdmin(user.getIsAdmin());
        vo.setMyInviteCode(user.getMyInviteCode());
        vo.setInviterId(user.getInviterId());
        vo.setVipExpireTime(user.getVipExpireTime());
        vo.setCoins(user.getCoins());
        vo.setShareUrl(SHARE_BASE_URL + user.getMyInviteCode());
        return vo;
    }
}