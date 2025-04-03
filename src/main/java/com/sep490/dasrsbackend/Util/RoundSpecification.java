package com.sep490.dasrsbackend.Util;

import com.sep490.dasrsbackend.model.entity.Round;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class RoundSpecification {

    // This method filters based on a keyword across multiple fields
    public Specification<Round> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return null;
            }
            String likeKeyword = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("roundName")), likeKeyword),
                    cb.like(cb.lower(root.get("tournament").get("tournamentName")), likeKeyword)
            );
        };
    }

    // This method filters based on the roundName field only
    public Specification<Round> hasRoundName(String roundName) {
        return (root, query, cb) -> {
            if (roundName == null || roundName.trim().isEmpty()) {
                return null;
            }
            String likeRoundName = "%" + roundName.toLowerCase() + "%";

            return cb.like(cb.lower(root.get("roundName")), likeRoundName);
        };
    }
}
