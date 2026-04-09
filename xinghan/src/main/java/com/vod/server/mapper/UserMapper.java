package com.vod.server.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vod.server.entity.domain.VodUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<VodUser> {
    @Select("SELECT id, username, password, status, is_admin, my_invite_code, inviter_id, " +
            "vip_expire_time, coins, is_deleted, create_time, update_time " +
            "FROM vod_user WHERE username = #{username}")
    VodUser selectByUsername(String username);

    @Select("SELECT id, username, status, is_deleted, coins " +
            "FROM vod_user WHERE my_invite_code = #{inviteCode}")
    VodUser selectByInviteCode(String inviteCode);
}