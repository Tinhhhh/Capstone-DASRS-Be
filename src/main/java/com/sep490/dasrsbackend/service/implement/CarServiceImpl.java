package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.TeamStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.EditCar;
import com.sep490.dasrsbackend.model.payload.request.NewCar;
import com.sep490.dasrsbackend.model.payload.response.CarResponse;
import com.sep490.dasrsbackend.model.payload.response.ListCarResponse;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.CarService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ListCarResponse getAllCars(int pageNo, int pageSize, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Car> cars = carRepository.findAll(pageable);

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
        Car car = modelMapper.map(newCar, Car.class);
        car.setEnabled(true);
        carRepository.save(car);

    }

    @Override
    public EditCar updateCar(Long id, EditCar editCar) {

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Car not found"));
        modelMapper.map(editCar, car);
        carRepository.save(car);

        return modelMapper.map(car, EditCar.class);
    }

}
