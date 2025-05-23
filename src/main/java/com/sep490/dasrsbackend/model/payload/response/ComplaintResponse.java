package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ComplaintResponse {

    private Long id;

    private String title;

    private String description;

    private String reply;

    private ComplaintStatus status;

    @JsonProperty("created_date")
    private Date createdDate;

    @JsonProperty("last_modified_date")
    private Date lastModifiedDate;

    @JsonProperty("match_id")
    private Long matchId;
}
