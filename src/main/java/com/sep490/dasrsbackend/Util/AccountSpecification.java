package com.sep490.dasrsbackend.Util;

import com.sep490.dasrsbackend.model.entity.Account;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class AccountSpecification {
    public static Specification<Account> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return null;
            }
            return cb.like(cb.lower(root.get("team").get("teamName")), "%" + keyword.toLowerCase() + "%");
        };
    }
}

