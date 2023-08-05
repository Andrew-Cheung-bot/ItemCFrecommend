package com.acproject.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateAccountDto implements Serializable {
    /**
     * 用户ID
     */
    private String uid;
    /**
     * 用户名
     */
    private String uname;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 用户性别
     */
    private String sex;
    /**
     * 用户激活状态
     */
    private String status;


}
