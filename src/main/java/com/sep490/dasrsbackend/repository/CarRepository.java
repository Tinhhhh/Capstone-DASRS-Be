package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {

    @Query("SELECT c FROM Car c WHERE c.isEnabled = true")
    List<Car> findCarsByEnabled();
}
