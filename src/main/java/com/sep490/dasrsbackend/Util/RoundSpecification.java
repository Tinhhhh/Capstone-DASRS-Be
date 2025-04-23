package com.sep490.dasrsbackend.Util;

import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.entity.TournamentTeam;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import com.sep490.dasrsbackend.model.enums.RoundStatusFilter;
import jakarta.persistence.criteria.Join;
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

    public static Specification<Round> belongsToTeam(Long teamId) {
        return (root, query, criteriaBuilder) -> {
            Join<Round, Tournament> tournamentJoin = root.join("tournament");

            Join<Tournament, TournamentTeam> tournamentTeamJoin = tournamentJoin.join("tournamentTeamList");

            return criteriaBuilder.equal(tournamentTeamJoin.get("team").get("id"), teamId);
        };
    }

    public static Specification<Round> belongsToTournament(Long tournamentId) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("tournament").get("id"), tournamentId);
        };
    }

    public Specification<Round> hasFilterStatus(RoundStatusFilter status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }
}
