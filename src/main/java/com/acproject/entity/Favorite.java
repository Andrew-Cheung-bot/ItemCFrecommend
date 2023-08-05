package com.acproject.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 用户喜爱分类实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class Favorite {

    private Integer fid;
    /**
     * 用户ID
     */
    private String uid;
    /**
     * 喜好图书主分类
     */
    private String classifymain;
    /**
     * 喜好图书副分类
     */
    private String classifysub;
}
