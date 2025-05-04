package com.sep490.dasrsbackend.Util;

import com.sep490.dasrsbackend.model.entity.Complaint;
import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class ComplaintSpecification {

    public Specification<Complaint> hasStatus(ComplaintStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public Specification<Complaint> hasTeamId(Long teamId) {
        return (root, query, cb) -> cb.equal(root.get("matchTeam").get("team").get("id"), teamId);
    }
}
