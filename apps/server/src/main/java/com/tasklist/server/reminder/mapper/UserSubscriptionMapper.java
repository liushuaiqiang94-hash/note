package com.tasklist.server.reminder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tasklist.server.reminder.entity.UserSubscriptionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSubscriptionMapper extends BaseMapper<UserSubscriptionEntity> {
}
