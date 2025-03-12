package com.sep490.dasrsbackend.Util;

import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.payload.response.RoundResponse;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class RoundSpecification {

    public Specification<Round> hasRoundName(String roundName) {
        return (root, query, cb) -> {
            if (roundName == null) return null;

            return cb.like(cb.lower(root.get("roundName")),
                    "%" + roundName.toLowerCase() + "%"
            );
        };
    }
}
