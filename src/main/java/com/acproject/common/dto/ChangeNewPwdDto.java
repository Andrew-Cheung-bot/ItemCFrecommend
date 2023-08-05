package com.acproject.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChangeNewPwdDto implements Serializable {
    private String uid;

    private String pass;

    private String checkPass;

    private Integer age;

    private String sex;
}
