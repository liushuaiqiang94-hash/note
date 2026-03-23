package com.tasklist.server.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tasklist.server.common.error.BusinessException;
import com.tasklist.server.common.error.ErrorCode;
import com.tasklist.server.config.WechatProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class WechatAuthClientImpl implements WechatAuthClient {

    private final WechatProperties wechatProperties;

    @Override
    public WechatSessionInfo code2Session(String code) {
        if (!StringUtils.hasText(wechatProperties.appId()) || !StringUtils.hasText(wechatProperties.appSecret())) {
            throw new BusinessException(ErrorCode.WECHAT_LOGIN_FAILED, "WeChat appId/appSecret is not configured");
        }

        Code2SessionResponse response = RestClient.builder()
                .baseUrl(wechatProperties.code2SessionUrl())
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("appid", wechatProperties.appId())
                        .queryParam("secret", wechatProperties.appSecret())
                        .queryParam("js_code", code)
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .retrieve()
                .body(Code2SessionResponse.class);

        if (response == null || !StringUtils.hasText(response.openId())) {
            throw new BusinessException(ErrorCode.WECHAT_LOGIN_FAILED, "Empty response from WeChat");
        }
        if (response.errCode() != null && response.errCode() != 0) {
            throw new BusinessException(ErrorCode.WECHAT_LOGIN_FAILED, response.errMsg());
        }
        return new WechatSessionInfo(response.openId(), response.unionId(), response.sessionKey());
    }

    private record Code2SessionResponse(
            @JsonProperty("openid") String openId,
            @JsonProperty("unionid") String unionId,
            @JsonProperty("session_key") String sessionKey,
            @JsonProperty("errcode") Integer errCode,
            @JsonProperty("errmsg") String errMsg
    ) {
    }
}
