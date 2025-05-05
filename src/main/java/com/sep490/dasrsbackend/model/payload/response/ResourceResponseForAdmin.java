package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResourceResponseForAdmin {

    @JsonProperty("resource_id")
    private Long id;

    @JsonProperty("resource_name")
    private String resourceName;

    @JsonProperty("resource_image")
    private String resourceImg;

    @JsonProperty("resource_type")
    private ResourceType resourceType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("is_enable")
    private boolean isEnable;

    @JsonProperty("last_modified_date")
    private String lastModifiedDate;

    @JsonProperty("created_date")
    private String createdDate;

}
