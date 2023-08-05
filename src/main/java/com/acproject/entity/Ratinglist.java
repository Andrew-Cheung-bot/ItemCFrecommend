package com.acproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 用户评分实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class Ratinglist {
    /**
     * 数据库自增主键禁止设值
     */
    @TableId(type = IdType.INPUT)
    private Integer rid;
    /**
     * 图书主键
     */
    private String eid;
    /**
     * 用户主键
     */
    private String uid;
    /**
     * 用户评分
     */
    private Double ratingvalue;
    /**
     * 用户评价
     */
    private String rdescribe;
    /**
     * 用户对象(联级查询用)
     */
    @TableField(exist = false)
    private User user;
    /**
     * 书名(联级查询用)
     */
    @TableField(exist = false)
    private Ebook ebook;
}
