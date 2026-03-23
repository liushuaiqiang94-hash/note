package com.tasklist.server.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tasklist.server.category.entity.TaskCategoryEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskCategoryMapper extends BaseMapper<TaskCategoryEntity> {
}
