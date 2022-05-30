package com.hzy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * User: hzy
 * Date: 2022/5/29
 * Time: 17:56
 * Description:
 */
@Data
@AllArgsConstructor
@Builder
public class Position {
    /**
     * 起始位置
     */
    private long start;
    /**
     * 长度
     */
    private long length;
}
