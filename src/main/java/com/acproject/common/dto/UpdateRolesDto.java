package com.acproject.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UpdateRolesDto implements Serializable {
    /**
     * 更改权限的用户ID
     */
    private String uid;
    /**
     * 用户角色数组
     */
    private List<String> roles;

}
