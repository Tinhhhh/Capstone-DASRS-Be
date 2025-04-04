package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.AccountCar;
import com.sep490.dasrsbackend.model.entity.AccountCarId;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.UpdateCarCustomization;
import com.sep490.dasrsbackend.model.payload.response.CarLoadoutResponse;
import com.sep490.dasrsbackend.repository.AccountCarRepository;
import com.sep490.dasrsbackend.service.AccountCarService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountCarServiceImpl implements AccountCarService {


    private final AccountCarRepository accountCarRepository;
    private final ModelMapper modelMapper;

    @Override
    public CarLoadoutResponse getCarLoadout(Long carId, UUID accountId) {

        AccountCar accountCar = accountCarRepository.findById(new AccountCarId(accountId, carId))
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Account car not found"));

        if (!accountCar.getCar().isEnabled()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Car not found");
        }

        return modelMapper.map(accountCar, CarLoadoutResponse.class);
    }

    @Override
    public CarLoadoutResponse updateCarLoadout(Long carId, UUID accountId, UpdateCarCustomization customization) {

        AccountCar accountCar = accountCarRepository.findById(new AccountCarId(accountId, carId))
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Account car not found"));

        if (!accountCar.getCar().isEnabled()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Car not found");
        }

        modelMapper.map(customization, accountCar);
        accountCarRepository.save(accountCar);
        return modelMapper.map(accountCar, CarLoadoutResponse.class);
    }
}
