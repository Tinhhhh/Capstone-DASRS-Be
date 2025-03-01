package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.request.NewEnvironment;
import com.sep490.dasrsbackend.model.payload.response.EnvironmentResponse;
import com.sep490.dasrsbackend.model.payload.response.ListEnvironment;

public interface EnvironmentService {
    void NewEnvironment(NewEnvironment request);

    EnvironmentResponse getEnvironment(Long id);

    ListEnvironment getAllEnvironment(int pageNo, int pageSize, String sortBy, String sortDir);
}
