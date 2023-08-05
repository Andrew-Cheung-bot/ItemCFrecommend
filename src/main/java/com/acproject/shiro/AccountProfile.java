package com.acproject.shiro;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccountProfile implements Serializable {
    /**
     * 用户主键
     */
    private String uid;
    /**
     * 用户名
     */
    private String uname;
    /**
     * 邮箱
     */
    private String email;
}
