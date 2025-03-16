package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.request.NewMap;
import com.sep490.dasrsbackend.model.payload.response.ListMapResponse;
import com.sep490.dasrsbackend.model.payload.response.MapResponse;

public interface RaceMapService {

    void newMap(NewMap request);

    void updateMap(Long id, NewMap request);

    MapResponse getMap(Long id);

    ListMapResponse getAllMap(int pageNo, int pageSize, String sortBy, String sortDir);

}
