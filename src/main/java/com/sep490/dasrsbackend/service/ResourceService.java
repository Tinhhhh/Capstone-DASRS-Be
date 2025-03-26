package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.request.NewResource;
import com.sep490.dasrsbackend.model.payload.response.ListResourceResponse;
import com.sep490.dasrsbackend.model.payload.response.ListResourceResponseForAdmin;
import com.sep490.dasrsbackend.model.payload.response.ResourceResponse;

public interface ResourceService {

    void newResource(NewResource request);

    void updateResource(Long id, NewResource request);

    ResourceResponse getResource(Long id);

    void changeResourceStatus(Long id, boolean enable);

    ListResourceResponseForAdmin getAllResourceForAdmin(int pageNo, int pageSize, String sortBy, String sortDirection);

    ListResourceResponse getAllResourceForAll(int pageNo, int pageSize, String sortBy, String sortDirection);
}
