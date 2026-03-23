package com.tasklist.server.reminder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("user_subscriptions")
public class UserSubscriptionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String scene;
    private String templateId;
    private String acceptStatus;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
