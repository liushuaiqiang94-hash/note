package com.tasklist.server.auth.vo;

public record LoginResponse(
        String accessToken,
        long expireInSeconds,
        Long userId,
        String nickName,
        String avatarUrl
) {
}
