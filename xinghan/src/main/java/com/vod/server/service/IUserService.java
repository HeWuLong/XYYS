package com.vod.server.service;
import com.vod.server.entity.dto.UserRegisterDTO;

public interface IUserService {
    void register(UserRegisterDTO dto);
}