package com.vod.server.controller;
import com.vod.server.entity.domain.VodUser;
import com.vod.server.entity.dto.UserRegisterDTO;
import com.vod.server.exception.ErrorCodeEnum;
import com.vod.server.exception.ServiceException;
import com.vod.server.mapper.UserMapper;
import com.vod.server.utils.Result;
import com.vod.server.service.IUserService;
import com.vod.server.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class LoginController {
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final IUserService userService;
    //登录
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        VodUser user = userMapper.selectByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new ServiceException(ErrorCodeEnum.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new ServiceException(ErrorCodeEnum.UNAUTHORIZED, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getIsAdmin() == 1);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("isAdmin", user.getIsAdmin() == 1);
        data.put("vipExpireTime", user.getVipExpireTime());
        return Result.success(data);
    }
    //注册
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功");
    }
    //登录请求
    public static class LoginRequest {
        @jakarta.validation.constraints.NotBlank(message = "用户名不能为空")
        private String username;
        @jakarta.validation.constraints.NotBlank(message = "密码不能为空")
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}