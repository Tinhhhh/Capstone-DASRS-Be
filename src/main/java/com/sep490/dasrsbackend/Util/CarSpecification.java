package com.sep490.dasrsbackend.Util;

import com.sep490.dasrsbackend.model.entity.Car;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class CarSpecification {

    public Specification<Car> isEnabled(boolean isEnabled) {
        return (root, query, cb) -> cb.equal(root.get("isEnabled"), isEnabled);
    }
}
