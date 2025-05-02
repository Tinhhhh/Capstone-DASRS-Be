package com.sep490.dasrsbackend.Util;

import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.entity.Role;
import com.sep490.dasrsbackend.model.enums.RoleFilter;
import jakarta.persistence.criteria.Join;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.units.qual.A;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class AccountSpecification {
    public static Specification<Account> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return null;
            }
            String likeKeyword = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), likeKeyword),
                    cb.like(cb.lower(root.get("lastName")), likeKeyword),
                    cb.like(cb.lower(root.get("team").get("teamName")), likeKeyword)
            );
        };
    }

    public Specification<Account> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) {
                return null;
            }
            String likeName = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), likeName),
                    cb.like(cb.lower(root.get("lastName")), likeName)
            );
        };
    }

    public Specification<Account> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.trim().isEmpty()) {
                return null;
            }
            String likeEmail = "%" + email.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("email")), likeEmail);
        };
    }

    public Specification<Account> hasRoleName(RoleFilter role){
        return (root, query, cb) -> {
            Join<Account, Role> roleJoin = root.join("role"); // Tên biến trong entity Account (kiểu Role)

            if (role == null || role == RoleFilter.ALL) {
                return cb.notEqual(roleJoin.get("roleName"), "ADMIN");
            }

            return cb.and(
                    cb.equal(roleJoin.get("roleName"), role.toString()),
                    cb.notEqual(roleJoin.get("roleName"), "ADMIN")
            );
        };
    }
    public static Specification<Account> fetchRole() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("role", jakarta.persistence.criteria.JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }
}

