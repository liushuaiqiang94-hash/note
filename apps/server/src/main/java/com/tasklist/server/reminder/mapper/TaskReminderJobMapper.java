package com.tasklist.server.reminder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tasklist.server.reminder.entity.TaskReminderJobEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskReminderJobMapper extends BaseMapper<TaskReminderJobEntity> {
}
