package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.AccountCar;
import com.sep490.dasrsbackend.model.entity.Car;
import com.sep490.dasrsbackend.model.payload.request.EditCar;
import com.sep490.dasrsbackend.model.payload.request.NewCar;
import com.sep490.dasrsbackend.model.payload.response.CarResponse;
import com.sep490.dasrsbackend.model.payload.response.ListCarResponse;
import com.sep490.dasrsbackend.repository.CarRepository;
import com.sep490.dasrsbackend.service.CarService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final ModelMapper modelMapper;

    @Override
    public ListCarResponse getAllCars(int pageNo, int pageSize, String sortBy, String sortDir) {
        return null;
    }

    @Override
    public CarResponse getCarById(Long id) {
        return null;
    }

    @Override
    public void createCar(NewCar newCar) {

        Car car = modelMapper.map(newCar, Car.class);
        car.setEnabled(true);
        carRepository.save(car);

    }

    @Override
    public EditCar updateCar(Long id, EditCar editCar) {
        return null;
    }

}
