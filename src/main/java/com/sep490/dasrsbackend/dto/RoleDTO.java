package com.sep490.dasrsbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoleDTO {

    @JsonProperty("role_id")
    private Long roleId;

    @JsonProperty("role_name")
    private String roleName;
}