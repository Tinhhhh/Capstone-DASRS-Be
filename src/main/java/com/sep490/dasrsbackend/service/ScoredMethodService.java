package com.sep490.dasrsbackend.service;


import com.sep490.dasrsbackend.model.enums.ScoredMethodStatus;
import com.sep490.dasrsbackend.model.payload.request.NewScoreMethod;
import com.sep490.dasrsbackend.model.payload.response.ListScoredMethod;
import com.sep490.dasrsbackend.model.payload.response.ScoredMethodResponse;

public interface ScoredMethodService {

    void createNewScoredMethod(NewScoreMethod newScoreMethod);

    ScoredMethodResponse getScoredMethod(Long scoredMethodId);

    void updateScoredMethod(Long scoredMethodId, NewScoreMethod newScoreMethod);

    ListScoredMethod getAllScoredMethods(int pageNo, int pageSize, String sortBy, String sortDirection);

    void changeStatus(Long scoredMethodId, ScoredMethodStatus status);
}
