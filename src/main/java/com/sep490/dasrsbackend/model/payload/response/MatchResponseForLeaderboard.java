package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.MatchForm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchResponseForLeaderboard {
    @JsonProperty(value = "match_id", index = 1)
    private Long matchId;

    @JsonProperty(value = "match_name", index = 2)
    private String matchName;

    @JsonProperty(value = "match_type", index = 3)
    private String matchType;

    @JsonProperty(value = "match_score", index = 4)
    private double matchScore;

    @JsonProperty(value = "match_form", index = 5)
    private MatchForm matchForm;

    @JsonProperty(value = "player_list", index = 6)
    private List<PlayerResponseForLeaderboard> playerList;

}
