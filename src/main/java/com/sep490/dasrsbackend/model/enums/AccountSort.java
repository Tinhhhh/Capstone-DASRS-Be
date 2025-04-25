package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum AccountSort {
    SORT_BY_ID_ASC("accountId", Sort.Direction.ASC),
    SORT_BY_ID_DESC("accountId", Sort.Direction.DESC),
    SORT_BY_LASTNAME_ASC("lastName", Sort.Direction.ASC),
    SORT_BY_LASTNAME_DESC("lastName", Sort.Direction.DESC),
    SORT_BY_FIRSTNAME_ASC("firstName", Sort.Direction.ASC),
    SORT_BY_FIRSTNAME_DESC("firstName", Sort.Direction.DESC),
    SORT_BY_EMAIL_ASC("email", Sort.Direction.ASC),
    SORT_BY_EMAIL_DESC("email", Sort.Direction.DESC),
    SORT_BY_ROLENAME_ASC("role.roleName", Sort.Direction.ASC),
    SORT_BY_ROLENAME_DESC("role.roleName", Sort.Direction.DESC);

    private final String field;
    private final Sort.Direction direction;

    AccountSort(String field, Sort.Direction direction) {
        this.field = field;
        this.direction = direction;
    }
}
