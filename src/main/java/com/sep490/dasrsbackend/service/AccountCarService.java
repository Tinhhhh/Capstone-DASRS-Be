package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.request.UpdateCarCustomization;
import com.sep490.dasrsbackend.model.payload.response.CarLoadoutResponse;

import java.util.UUID;

public interface AccountCarService {

    CarLoadoutResponse getCarLoadout(Long carId, UUID accountId);

    CarLoadoutResponse updateCarLoadout(Long carId, UUID accountId, UpdateCarCustomization customization);

}
