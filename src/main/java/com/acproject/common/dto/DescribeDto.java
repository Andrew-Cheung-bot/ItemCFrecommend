package com.acproject.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DescribeDto implements Serializable {
    /**
     * 用户评分
     */
    private Double rate;
    /**
     * 用户评测
     */
    private String user_comment;
    /**
     * 评测书籍id
     */
    private String eid;
    /**
     * 用户id
     */
    private String uid;

}
