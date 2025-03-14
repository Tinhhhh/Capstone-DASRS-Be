package com.sep490.dasrsbackend.Util;

import com.sep490.dasrsbackend.model.entity.Tournament;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class TournamentSpecification {

    public Specification<Tournament> hasTournamentName(String tournamentName) {
        return (root, query, cb) -> {
            if (tournamentName == null) return null;

            return cb.like(cb.lower(root.get("tournamentName")),
                    "%" + tournamentName.toLowerCase() + "%"
            );
        };
    }

}
