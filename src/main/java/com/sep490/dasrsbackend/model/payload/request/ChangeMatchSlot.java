package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangeMatchSlot {

    @JsonProperty("account_id")
    private UUID accountId;

    @JsonProperty("start_date")
    private Date startDate;

//    @JsonProperty("end_date")
//    private Date endDate;
}
