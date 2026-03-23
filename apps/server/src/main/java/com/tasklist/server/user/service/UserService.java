package com.tasklist.server.user.service;

import com.tasklist.server.common.auth.CurrentUserProvider;
import com.tasklist.server.common.error.BusinessException;
import com.tasklist.server.common.error.ErrorCode;
import com.tasklist.server.user.entity.UserEntity;
import com.tasklist.server.user.mapper.UserMapper;
import com.tasklist.server.user.vo.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final CurrentUserProvider currentUserProvider;

    public UserProfileResponse getCurrentProfile() {
        Long userId = currentUserProvider.requireCurrentUserId();
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "User not found");
        }
        return new UserProfileResponse(user.getId(), user.getNickname(), user.getAvatarUrl());
    }
}
