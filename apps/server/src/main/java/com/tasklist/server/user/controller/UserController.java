package com.tasklist.server.user.controller;

import com.tasklist.server.common.api.ApiResponse;
import com.tasklist.server.user.service.UserService;
import com.tasklist.server.user.vo.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/v1")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getCurrentUser() {
        return ApiResponse.success(userService.getCurrentProfile());
    }
}
