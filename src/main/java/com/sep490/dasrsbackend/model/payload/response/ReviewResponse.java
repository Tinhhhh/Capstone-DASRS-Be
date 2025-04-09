package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;
import lombok.Data;

import java.util.Date;

@Data
public class ReviewResponse {

    private Long id;

    private String title;

    private String description;

    private String reply;

    private ReviewStatus status;

    @JsonProperty("created_date")
    private Date createdDate;

    @JsonProperty("last_modified_date")
    private Date lastModifiedDate;
}
