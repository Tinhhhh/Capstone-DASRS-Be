package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.response.MatchResponse;

import java.util.List;

public interface TeamService {

    List<MatchResponse> getUpcomingMatches(Long teamId);

}
