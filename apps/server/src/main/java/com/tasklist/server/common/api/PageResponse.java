package com.tasklist.server.common.api;

import java.util.List;

public record PageResponse<T>(long pageNum, long pageSize, long total, List<T> list) {
}
