package com.tasklist.server.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tasklist.server.task.entity.TaskEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<TaskEntity> {
}
