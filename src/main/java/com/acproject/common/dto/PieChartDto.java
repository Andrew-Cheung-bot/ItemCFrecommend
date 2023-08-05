package com.acproject.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PieChartDto implements Serializable {
    /**
     * 评分值
     */
    private Integer value;
    /**
     * 评分类别
     */
    private String name;
}
