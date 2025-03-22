package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.enums.MapStatus;
import com.sep490.dasrsbackend.model.enums.ResourceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewResource {

    @JsonProperty("resource_name")
    @Size(max = 200, message = "Map name no more than 200 characters")
    private String mapName;

    @JsonProperty("resource_image")
    private String mapImage;

    @JsonProperty("resource_type")
    private ResourceType resourceType;

    private String description;

}
