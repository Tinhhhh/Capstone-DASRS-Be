package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.EditCar;
import com.sep490.dasrsbackend.model.payload.request.NewCar;
import com.sep490.dasrsbackend.service.CarService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cars")
@Tag(name = "Car", description = "Car API for unity")
public class CarController {

    private final CarService carService;

    @PostMapping
    public ResponseEntity<Object> newAccount(@RequestBody @Valid NewCar newCar) {
        carService.createCar(newCar);
        return ResponseBuilder.responseBuilder(
                HttpStatus.CREATED, "Create new car successfully"
        );
    }

    @PutMapping("/{carId}")
    public ResponseEntity<Object> editCar(@PathVariable Long carId, @RequestBody @Valid EditCar editCar) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Edit car successfully", carService.updateCar(carId, editCar)
        );
    }

    @GetMapping("/{carId}")
    public ResponseEntity<Object> getCar(@PathVariable Long carId) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Get car successfully", carService.getCarById(carId)
        );
    }

    @GetMapping
    public ResponseEntity<Object> getAllCars(
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection
    ) {
        return ResponseBuilder.responseBuilderWithData(
                HttpStatus.OK, "Get all cars successfully",
                carService.getAllCars(pageNo, pageSize, sortBy, sortDirection)
        );
    }
}
