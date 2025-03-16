package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.MapStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapResponse {

    @JsonProperty("map_id")
    private Long mapId;

    @JsonProperty("map_name")
    private String mapName;

    @JsonProperty("map_image")
    private String mapImage;

    @JsonProperty("status")
    private MapStatus status;

    private String source;

}
