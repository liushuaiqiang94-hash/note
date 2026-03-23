package com.tasklist.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasklist.server.auth.service.JwtTokenService;
import com.tasklist.server.auth.service.WechatAuthClient;
import com.tasklist.server.user.entity.UserEntity;
import com.tasklist.server.user.mapper.UserMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtTokenService jwtTokenService;

    @Autowired
    protected UserMapper userMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @MockBean
    protected WechatAuthClient wechatAuthClient;

    @BeforeEach
    void cleanupDatabase() {
        jdbcTemplate.execute("DELETE FROM task_reminder_jobs");
        jdbcTemplate.execute("DELETE FROM user_subscriptions");
        jdbcTemplate.execute("DELETE FROM tasks");
        jdbcTemplate.execute("DELETE FROM task_categories");
        jdbcTemplate.execute("DELETE FROM users");
    }

    protected UserEntity createUser(String openid, String nickName) {
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setOpenid(openid);
        user.setNickname(nickName);
        user.setAvatarUrl("https://example.com/avatar.png");
        user.setStatus("ACTIVE");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setLastLoginAt(now);
        userMapper.insert(user);
        return user;
    }

    protected String bearerToken(UserEntity user) {
        return "Bearer " + jwtTokenService.generateToken(user.getId());
    }
}
