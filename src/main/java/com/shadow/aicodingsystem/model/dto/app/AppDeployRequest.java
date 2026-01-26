package com.shadow.aicodingsystem.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用ID
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}
