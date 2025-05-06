package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardWithMatchDetailsResponse {

    @JsonProperty(value = "round_id", index = 1)
    private Long roundId;

    @JsonProperty(value = "round_name", index = 2)
    private String roundName;

    @JsonProperty(value = "content", index = 3)
    private List<LeaderboardWithMatchDetails> content;

    @JsonProperty(value = "page_no", index = 4)
    private int pageNo;

    @JsonProperty(value = "page_size", index = 5)
    private int pageSize;

    @JsonProperty(value = "total_elements", index = 6)
    private long totalElements;

    @JsonProperty(value = "total_pages", index = 7)
    private int totalPages;

    @JsonProperty(value = "last", index = 8)
    private boolean last;
}
