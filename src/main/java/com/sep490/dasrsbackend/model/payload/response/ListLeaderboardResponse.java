package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.entity.Leaderboard;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListLeaderboardResponse {

    @JsonProperty(value = "content", index = 1)
    private List<LeaderboardResponse> content;

    @JsonProperty(value = "page_no", index = 2)
    private int pageNo;
    @JsonProperty(value = "page_size", index = 3)
    private int pageSize;
    @JsonProperty(value = "total_elements", index = 4)
    private long totalElements;
    @JsonProperty(value = "total_pages", index = 5)
    private int totalPages;
    @JsonProperty(value = "last", index = 6)
    private boolean last;
}
