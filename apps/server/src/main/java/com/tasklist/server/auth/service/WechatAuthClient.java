package com.tasklist.server.auth.service;

public interface WechatAuthClient {

    WechatSessionInfo code2Session(String code);
}
