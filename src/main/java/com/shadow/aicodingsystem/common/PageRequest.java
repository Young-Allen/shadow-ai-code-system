package com.shadow.aicodingsystem.common;

import lombok.Data;

@Data
public class PageRequest {
    /**
     * 当前页
     */
    private Integer pageNum = 1;
    /**
     * 每页显示条数
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式(默认降序)
     */
    private String sortOrder = "descend";
}
