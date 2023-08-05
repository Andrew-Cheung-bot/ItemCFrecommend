package com.acproject.common.dto;


import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class RegistDto implements Serializable {
    /**
     * 用户主键
     */
    @NotBlank(message = "用户ID不能为空")
    private String uid;
    /**
     * 用户名
     */
    private String uname;
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    /**
     * 密码
     */
    @NotBlank(message = "用户密码不能为空")
    private String password;
    /**
     * 性别
     */
    private String sex;
    /**
     * 年龄
     */
    private Integer age;
}
