package com.sep490.dasrsbackend.service;


import com.sep490.dasrsbackend.model.payload.request.NewMatchType;
import com.sep490.dasrsbackend.model.payload.response.ListMatchType;
import com.sep490.dasrsbackend.model.payload.response.MatchTypeResponse;

public interface MatchTypeService {
    void newMatchType(NewMatchType newMatchType);
    MatchTypeResponse getMatchType(Long id);
    ListMatchType getAllMatchType(int pageNo, int pageSize, String sortBy, String sortDir);
    void updateMatchType(Long id, NewMatchType newMatchType);
    void deleteMatchType(Long id);
}
