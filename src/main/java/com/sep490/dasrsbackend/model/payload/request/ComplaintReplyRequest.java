package com.sep490.dasrsbackend.model.payload.request;

import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComplaintReplyRequest {

    private String reply;

    private ComplaintStatus status;
}
