package com.tasklist.server.auth.service;

public record WechatSessionInfo(String openId, String unionId, String sessionKey) {
}
