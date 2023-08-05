package com.acproject.common.dto;

        import lombok.Data;
        import javax.validation.constraints.NotBlank;
        import java.io.Serializable;

@Data
public class LoginDto implements Serializable {
    /**
     * 用户主键
     */
    @NotBlank(message = "用户ID不能为空")
    private String uid;
    /**C
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    /**
     * 激活状态
     */
    private String status;
}
