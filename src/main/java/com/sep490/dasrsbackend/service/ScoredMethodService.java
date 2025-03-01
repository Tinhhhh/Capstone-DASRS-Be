package com.sep490.dasrsbackend.service;


import com.sep490.dasrsbackend.model.payload.request.NewScoreMethod;
import com.sep490.dasrsbackend.model.payload.response.ScoredMethodResponse;

public interface ScoredMethodService {

    void createNewScoredMethod(NewScoreMethod newScoreMethod);

    ScoredMethodResponse getScoredMethod(Long scoredMethodId);

    void updateScoredMethod(Long scoredMethodId, NewScoreMethod newScoreMethod);

}
