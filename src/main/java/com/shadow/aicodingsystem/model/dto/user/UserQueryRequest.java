package com.shadow.aicodingsystem.model.dto.user;

import com.shadow.aicodingsystem.common.PageRequest;
import lombok.Data;

import java.io.Serializable;


@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    private Long id;

    private String userName;

    private String userAccount;

    private String userProfile;

    /**
     * 用户角色：admin/user/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}