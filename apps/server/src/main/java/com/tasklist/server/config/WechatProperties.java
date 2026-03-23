package com.tasklist.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.wechat")
public record WechatProperties(
        String appId,
        String appSecret,
        String code2SessionUrl
) {
}
