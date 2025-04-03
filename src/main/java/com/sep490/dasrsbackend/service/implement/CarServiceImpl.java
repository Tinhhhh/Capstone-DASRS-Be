package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.payload.request.EditCar;
import com.sep490.dasrsbackend.model.payload.request.NewCar;
import com.sep490.dasrsbackend.model.payload.response.CarResponse;
import com.sep490.dasrsbackend.model.payload.response.ListCarResponse;
import com.sep490.dasrsbackend.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    @Override
    public ListCarResponse getAllCars(int pageNo, int pageSize, String sortBy, String sortDir) {
        return null;
    }

    @Override
    public CarResponse getCarById(Long id) {
        return null;
    }

    @Override
    public NewCar createCar(NewCar newCar) {
        return null;
    }

    @Override
    public EditCar updateCar(Long id, EditCar editCar) {
        return null;
    }

}
