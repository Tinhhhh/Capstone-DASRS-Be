package com.sep490.dasrsbackend.service;


import com.sep490.dasrsbackend.model.payload.request.EditCar;
import com.sep490.dasrsbackend.model.payload.request.NewCar;
import com.sep490.dasrsbackend.model.payload.response.CarResponse;
import com.sep490.dasrsbackend.model.payload.response.ListCarResponse;

public interface CarService {

    ListCarResponse getAllCars(int pageNo, int pageSize, String sortBy, String sortDir, Boolean isActive);

    CarResponse getCarById(Long id);

    void createCar(NewCar newCar);

    EditCar updateCar(Long id, EditCar editCar);
}
