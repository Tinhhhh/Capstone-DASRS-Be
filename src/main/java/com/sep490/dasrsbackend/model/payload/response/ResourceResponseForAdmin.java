package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.MapStatus;
import com.sep490.dasrsbackend.model.enums.ResourceType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResourceResponseForAdmin {

    @JsonProperty("resource_id")
    private Long resourceId;

    @JsonProperty("resource_name")
    private String resourceName;

    @JsonProperty("resource_image")
    private String resourceImage;

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
