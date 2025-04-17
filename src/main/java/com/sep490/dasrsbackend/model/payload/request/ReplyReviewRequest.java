package com.sep490.dasrsbackend.model.payload.request;

import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import lombok.Data;

@Data
public class ReplyReviewRequest {

    private String reply;

    private ComplaintStatus status;
}
