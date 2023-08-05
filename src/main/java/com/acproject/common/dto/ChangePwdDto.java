package com.acproject.common.dto;

import lombok.Data;
import org.springframework.http.converter.json.GsonBuilderUtils;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class ChangePwdDto implements Serializable {
    /**
     * 用户主键
     */
    @NotBlank(message = "用户ID不能为空")
    private String uid;
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
}
