package com.tasklist.server.auth.service;

import com.tasklist.server.auth.dto.WechatLoginRequest;
import com.tasklist.server.auth.vo.LoginResponse;
import com.tasklist.server.user.entity.UserEntity;
import com.tasklist.server.user.mapper.UserMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WechatAuthClient wechatAuthClient;
    private final UserMapper userMapper;
    private final JwtTokenService jwtTokenService;

    @Transactional
    public LoginResponse wechatLogin(WechatLoginRequest request) {
        WechatSessionInfo sessionInfo = wechatAuthClient.code2Session(request.code());
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = userMapper.selectOneByOpenid(sessionInfo.openId());

        if (user == null) {
            user = new UserEntity();
            user.setOpenid(sessionInfo.openId());
            user.setUnionid(sessionInfo.unionId());
            user.setNickname(request.nickName());
            user.setAvatarUrl(request.avatarUrl());
            user.setStatus("ACTIVE");
            user.setLastLoginAt(now);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            userMapper.insert(user);
        } else {
            user.setUnionid(sessionInfo.unionId());
            user.setNickname(request.nickName());
            user.setAvatarUrl(request.avatarUrl());
            user.setLastLoginAt(now);
            user.setUpdatedAt(now);
            userMapper.updateById(user);
        }

        String accessToken = jwtTokenService.generateToken(user.getId());
        return new LoginResponse(
                accessToken,
                jwtTokenService.getAccessTokenExpireSeconds(),
                user.getId(),
                user.getNickname(),
                user.getAvatarUrl()
        );
    }
}
