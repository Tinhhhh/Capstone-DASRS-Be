package com.sep490.dasrsbackend.Util;

import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@UtilityClass
public class RoundSpecification {

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

    public Specification<Round> betweenStartAndEndDate(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null || end == null) {
                return null;
            }
            return cb.between(root.get("startDate"), start, end);
        };
    }

    public Specification<Round> hasStatus(RoundStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public Specification<Round> belongsToTeam(Long teamId) {
        return (root, query, cb) -> {
            if (teamId == null) {
                return null;
            }
            return cb.equal(root.join("team").get("id"), teamId);
        };
    }
}
