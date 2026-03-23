package com.tasklist.server;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tasklist.server.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TaskFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateUpdateDeleteAndRestoreTask() throws Exception {
        UserEntity user = createUser("openid-task-1", "TaskUser");
        String authHeader = bearerToken(user);

        String categoryResponse = mockMvc.perform(post("/api/app/v1/categories")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"工作",
                                  "color":"#1677ff",
                                  "sortNo":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("工作")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long categoryId = objectMapper.readTree(categoryResponse).path("data").path("id").asLong();

        String taskResponse = mockMvc.perform(post("/api/app/v1/tasks")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "title":"完成日报",
                                  "description":"下班前提交",
                                  "priority":"HIGH",
                                  "dueAt":"2030-01-01T10:00:00",
                                  "remindAt":"2030-01-01T09:00:00",
                                  "repeatType":"NONE"
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("TODO")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long taskId = objectMapper.readTree(taskResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/app/v1/tasks")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", is(1)))
                .andExpect(jsonPath("$.data.list", hasSize(1)));

        mockMvc.perform(patch("/api/app/v1/tasks/{taskId}/status", taskId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status":"DONE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("DONE")));

        mockMvc.perform(delete("/api/app/v1/tasks/{taskId}", taskId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/app/v1/tasks/recycle-bin")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total", is(1)));

        mockMvc.perform(post("/api/app/v1/tasks/{taskId}/restore", taskId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("DONE")));

        mockMvc.perform(delete("/api/app/v1/categories/{categoryId}", categoryId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/app/v1/tasks/{taskId}", taskId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryId").doesNotExist());
    }
}
