package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum PlayerSort {
    SORT_BY_ID_ASC("accountId", Sort.Direction.ASC),
    SORT_BY_ID_DESC("accountId", Sort.Direction.DESC),
    SORT_BY_LASTNAME_ASC("lastName", Sort.Direction.ASC),
    SORT_BY_LASTNAME_DESC("lastName", Sort.Direction.DESC),
    SORT_BY_FIRSTNAME_ASC("firstName", Sort.Direction.ASC),
    SORT_BY_FIRSTNAME_DESC("firstName", Sort.Direction.DESC),
    SORT_BY_EMAIL_ASC("email", Sort.Direction.ASC),
    SORT_BY_EMAIL_DESC("email", Sort.Direction.DESC),
    SORT_BY_TEAMNAME_ASC("team.teamName", Sort.Direction.ASC),
    SORT_BY_TEAMNAME_DESC("team.teamName", Sort.Direction.DESC);

    private final String field;
    private final Sort.Direction direction;

    PlayerSort(String field, Sort.Direction direction) {
        this.field = field;
        this.direction = direction;
    }
}
