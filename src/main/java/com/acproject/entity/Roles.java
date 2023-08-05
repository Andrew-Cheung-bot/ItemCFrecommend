package com.acproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class Roles {
    /**
     * 用户Id
     */
    @TableId(type = IdType.INPUT)
    private String uid;
    /**
     * 用户含有角色
     */
    private String role;
}
