package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.enums.MapStatus;
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
public class NewMap {

    @JsonProperty("map_name")
    @Size(max = 200, message = "Map name no more than 200 characters")
    private String mapName;

    @JsonProperty("map_image")
    private String mapImage;

    @JsonProperty("status")
    private MapStatus status;

    private String source;

}
