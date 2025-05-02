package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.request.EditEnvironment;
import com.sep490.dasrsbackend.model.payload.request.NewEnvironment;
import com.sep490.dasrsbackend.model.payload.response.EnvironmentResponse;
import com.sep490.dasrsbackend.model.payload.response.ListEnvironment;

public interface EnvironmentService {
    void newEnvironment(NewEnvironment request);

    EnvironmentResponse getEnvironment(Long id);

    ListEnvironment getAllEnvironment(int pageNo, int pageSize, String sortBy, String sortDir);

    void updateEnvironment(Long id, EditEnvironment request);

    void deleteEnvironment(Long id);

    ListEnvironment getAllActiveEnvironments(int pageNo, int pageSize, String sortBy, String sortDir);

    EnvironmentResponse getActiveEnvironment(Long id);
}
