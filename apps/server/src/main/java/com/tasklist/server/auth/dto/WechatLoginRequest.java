package com.tasklist.server.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record WechatLoginRequest(
        @NotBlank String code,
        @NotBlank String nickName,
        String avatarUrl
) {
}
