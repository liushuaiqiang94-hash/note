package com.tasklist.server;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tasklist.server.auth.service.WechatSessionInfo;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldLoginAndFetchProfile() throws Exception {
        when(wechatAuthClient.code2Session("valid-code"))
                .thenReturn(new WechatSessionInfo("openid-auth-1", null, "session"));

        String response = mockMvc.perform(post("/api/app/v1/auth/wechat/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"valid-code",
                                  "nickName":"Alice",
                                  "avatarUrl":"https://example.com/a.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = objectMapper.readTree(response).path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/app/v1/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickName", is("Alice")));
    }
}
