package com.acproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class User {
    /**
     * 用户主键
     */
    @NotBlank(message = "用户ID不能为空")
    @TableId(type = IdType.INPUT)
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
    private String password;
    /**
     * 性别
     */
    private String sex;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 激活码
     */
    private String code;
    /**
     * 激活状态
     */
    private String status;
    /**
     * 是否处于登录状态
     */
    private Integer login;

}
