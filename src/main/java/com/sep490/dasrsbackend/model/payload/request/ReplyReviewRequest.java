package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;
import lombok.Data;

@Data
public class ReplyReviewRequest {

    private String reply;

    private ReviewStatus status;
}
