package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum MatchSort {
    SORT_BY_ID_ASC("id", Sort.Direction.ASC),
    SORT_BY_ID_DESC("id", Sort.Direction.DESC),
    SORT_BY_NAME_ASC("matchName", Sort.Direction.ASC),
    SORT_BY_NAME_DESC("matchName", Sort.Direction.DESC),
    SORT_BY_CREATED_ASC("createdDate", Sort.Direction.ASC),
    SORT_BY_CREATED_DESC("createdDate", Sort.Direction.DESC),
    SORT_BY_START_ASC("startDate", Sort.Direction.ASC),
    SORT_BY_START_DESC("startDate", Sort.Direction.DESC),
    SORT_BY_END_ASC("endDate", Sort.Direction.ASC),
    SORT_BY_END_DESC("endDate", Sort.Direction.DESC);

    private final String field;
    private final Sort.Direction direction;

    MatchSort(String field, Sort.Direction direction) {
        this.field = field;
        this.direction = direction;
    }
}
