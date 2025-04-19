package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class ComplaintResponseDetails {

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

    @JsonProperty("team_id")
    private Long teamId;

    @JsonProperty("account_id")
    private UUID accountId;

    @JsonProperty("match_team_id")
    private Long matchTeamId;
}
