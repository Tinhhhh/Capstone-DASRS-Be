package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.AccountCar;
import com.sep490.dasrsbackend.model.entity.AccountCarId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountCarRepository extends JpaRepository<AccountCar, AccountCarId> {

}
