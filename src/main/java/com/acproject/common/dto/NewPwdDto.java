package com.acproject.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class NewPwdDto implements Serializable {
    /**
     * 密码
     */
    @NotBlank(message = "用户密码不能为空")
    private String password;
}
