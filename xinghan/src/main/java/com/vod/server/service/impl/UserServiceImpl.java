package com.vod.server.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vod.server.entity.domain.VodUser;
import com.vod.server.entity.dto.UserRegisterDTO;
import com.vod.server.exception.ErrorCodeEnum;
import com.vod.server.exception.ServiceException;
import com.vod.server.mapper.UserMapper;
import com.vod.server.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO dto) {
        String username = dto.getUsername().trim();
        String rawPassword = dto.getPassword();
        String inviteCode = dto.getInviteCode();
        // 校验用户名是否已存在
        long count = userMapper.selectCount(new LambdaQueryWrapper<VodUser>()
                .eq(VodUser::getUsername, username)
                .eq(VodUser::getIsDeleted, 0));
        if (count > 0) {
            throw new ServiceException(ErrorCodeEnum.BIZ_ERROR, "该账号已被注册");
        }
        // 2. 处理邀请人ID
        Long inviterId = null;
        if (inviteCode != null && !inviteCode.trim().isEmpty()) {
            VodUser inviter = userMapper.selectOne(new LambdaQueryWrapper<VodUser>()
                    .eq(VodUser::getMyInviteCode, inviteCode.trim())
                    .eq(VodUser::getStatus, 0)      // 邀请人必须状态正常
                    .eq(VodUser::getIsDeleted, 0));
            if (inviter != null) {
                inviterId = inviter.getId();
                log.info("用户注册使用邀请码 {}，邀请人ID: {}", inviteCode, inviterId);
            } else {
                log.warn("无效的邀请码: {}", inviteCode);
                // 无效邀请码不抛出异常，仅忽略，不绑定上级
            }
        }
        // 3. 生成唯一邀请码（8位大写字母+数字）
        String myInviteCode = generateUniqueInviteCode();
        // 4. 创建新用户
        VodUser newUser = new VodUser();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setStatus(0);               // 0-正常
        newUser.setIsAdmin(0);              // 0-普通用户
        newUser.setMyInviteCode(myInviteCode);
        newUser.setInviterId(inviterId != null ? inviterId : 0L);
        newUser.setVipExpireTime(null);
        newUser.setCoins(10);               // 初始金币 10
        newUser.setIsDeleted(0);
        newUser.setCreateTime(LocalDateTime.now());
        newUser.setUpdateTime(LocalDateTime.now());

        userMapper.insert(newUser);

        // 5. 奖励逻辑：若有邀请人，给邀请人增加 5 金币
        if (inviterId != null) {
            int updated = userMapper.update(null,
                    new LambdaUpdateWrapper<VodUser>()
                            .eq(VodUser::getId, inviterId)
                            .setSql("coins = coins + 5")
                            .set(VodUser::getUpdateTime, LocalDateTime.now()));
            if (updated > 0) {
                log.info("邀请人 ID {} 获得 5 金币奖励（新用户 {} 注册）", inviterId, username);
            } else {
                log.warn("邀请人 ID {} 金币增加失败，可能用户已被删除或封禁", inviterId);
            }
        }
    }
    private String generateUniqueInviteCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
            }
            code = sb.toString();
        } while (userMapper.selectCount(new LambdaQueryWrapper<VodUser>()
                .eq(VodUser::getMyInviteCode, code)) > 0);
        return code;
    }
}