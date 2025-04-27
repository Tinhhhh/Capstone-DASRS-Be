package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.CarSpecification;
import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.Car;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.EditCar;
import com.sep490.dasrsbackend.model.payload.request.NewCar;
import com.sep490.dasrsbackend.model.payload.response.CarResponse;
import com.sep490.dasrsbackend.model.payload.response.ListCarResponse;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.CarService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final ModelMapper modelMapper;
    private final AccountCarRepository accountCarRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final AccountRepository accountRepository;

    @Override
    public ListCarResponse getAllCars(int pageNo, int pageSize, String sortBy, String sortDir, Boolean isEnabled) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Car> cars;
        Specification<Car> specification;
        if (isEnabled != null) {
            specification = CarSpecification.isEnabled(isEnabled);
            cars = carRepository.findAll(specification, pageable);
        } else {
            cars = carRepository.findAll(pageable);
        }

        List<CarResponse> carResponses = new ArrayList<>();

        for (Car car : cars.getContent()) {
            CarResponse carResponse = getCarById(car.getId());
            carResponses.add(carResponse);
        }

        ListCarResponse listCarResponse = new ListCarResponse();
        listCarResponse.setContent(carResponses);
        listCarResponse.setPageNo(cars.getNumber());
        listCarResponse.setPageSize(cars.getSize());
        listCarResponse.setTotalElements(cars.getTotalElements());
        listCarResponse.setTotalPages(cars.getTotalPages());
        listCarResponse.setLast(cars.isLast());

        return listCarResponse;
    }

    @Override
    public CarResponse getCarById(Long id) {

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Car not found"));

        CarResponse carResponse = modelMapper.map(car, CarResponse.class);
        carResponse.setLastModifiedDate(DateUtil.formatTimestamp(car.getLastModifiedDate()));
        carResponse.setCreatedDate(DateUtil.formatTimestamp(car.getCreatedDate()));

        return carResponse;
    }

    @Transactional
    @Override
    public void createCar(NewCar newCar) {

        if (newCar.getShiftUpRPM() > newCar.getMaxEngineRPM() || newCar.getShiftUpRPM() < newCar.getMinEngineRPM()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Shift up RPM must be between min engine RPM and max engine RPM");
        }

        if (newCar.getShiftDownRPM() > newCar.getMaxEngineRPM() || newCar.getShiftDownRPM() < newCar.getMinEngineRPM()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Shift down RPM must be between min engine RPM and max engine RPM");
        }

        if (newCar.getMaxTorqueAsNM() < newCar.getMinEngineRPM() || newCar.getMaxTorqueAsNM() > newCar.getMaxEngineRPM()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Max torque as NM must be between min engine RPM and max engine RPM");
        }

        if (newCar.getMaxBrakeTorque() < newCar.getMinEngineRPM() || newCar.getMaxBrakeTorque() > newCar.getMaxEngineRPM()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Max torque as NM must be between min engine RPM and max engine RPM");
        }
        Car car = modelMapper.map(newCar, Car.class);
        car.setEnabled(true);
        carRepository.save(car);

    }

    @Override
    public EditCar updateCar(Long id, EditCar editCar) {

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Car not found"));

        if (editCar.getShiftUpRPM() > editCar.getMaxEngineRPM() || editCar.getShiftUpRPM() < editCar.getMinEngineRPM()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Shift up RPM must be between min engine RPM and max engine RPM");
        }

        if (editCar.getShiftDownRPM() > editCar.getMaxEngineRPM() || editCar.getShiftDownRPM() < editCar.getMinEngineRPM()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Shift down RPM must be between min engine RPM and max engine RPM");
        }

        if (editCar.getMaxTorqueAsNM() < editCar.getMinEngineRPM() || editCar.getMaxTorqueAsNM() > editCar.getMaxEngineRPM()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Max torque as NM must be between min engine RPM and max engine RPM");
        }

        if (editCar.getMaxBrakeTorque() < editCar.getMinEngineRPM() || editCar.getMaxBrakeTorque() > editCar.getMaxEngineRPM()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Max torque as NM must be between min engine RPM and max engine RPM");
        }

        modelMapper.map(editCar, car);
        carRepository.save(car);

        return modelMapper.map(car, EditCar.class);
    }

}
