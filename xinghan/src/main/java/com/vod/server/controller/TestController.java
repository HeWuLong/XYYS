package com.vod.server.controller;
import com.vod.server.utils.Result;
import com.vod.server.security.context.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class TestController {
    @GetMapping("/current-user")
    public Result<Long> getCurrentUserId() {
        return Result.success(UserContext.getUserId());
    }
}