package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.MapStatus;
import com.sep490.dasrsbackend.model.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResourceResponse {

    @JsonProperty("resource_id")
    private Long id;

    @JsonProperty("resource_name")
    private String resourceName;

    @JsonProperty("resource_image")
    private String resourceImage;

    @JsonProperty("resource_type")
    private ResourceType resourceType;

    @JsonProperty("description")
    private String description;

}
