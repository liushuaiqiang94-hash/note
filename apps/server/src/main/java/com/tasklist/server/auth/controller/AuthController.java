package com.tasklist.server.auth.controller;

import com.tasklist.server.auth.dto.WechatLoginRequest;
import com.tasklist.server.auth.service.AuthService;
import com.tasklist.server.auth.vo.LoginResponse;
import com.tasklist.server.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/wechat/login")
    public ApiResponse<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success(authService.wechatLogin(request));
    }
}
