package com.sep490.dasrsbackend.Util;

import com.sep490.dasrsbackend.model.entity.Match;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class MatchSpecification {

    public Specification<Match> hasMatchName(String matchName) {
        return (root, query, cb) -> {
            if (matchName == null) return null;

            return cb.like(cb.lower(root.get("matchName")),
                    "%" + matchName.toLowerCase() + "%"
            );
        };
    }

    public Specification<Match> hasRoundId(Long roundId) {
        return (root, query, cb) -> {
            if (roundId == null) return null;

            return cb.equal(root.get("round").get("id"), roundId);
        };
    }
}
