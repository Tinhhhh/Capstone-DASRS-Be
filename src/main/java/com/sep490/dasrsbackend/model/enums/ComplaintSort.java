package com.sep490.dasrsbackend.model.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum ComplaintSort {
    SORT_BY_ID_ASC("id", Sort.Direction.ASC),
    SORT_BY_ID_DESC("id", Sort.Direction.DESC),
    SORT_BY_CREATED_ASC("createdDate", Sort.Direction.ASC),
    SORT_BY_CREATED_DESC("createdDate", Sort.Direction.DESC),
    SORT_BY_STATUS_ASC("status", Sort.Direction.ASC),
    SORT_BY_STATUS_DESC("status", Sort.Direction.DESC);

    private final String field;
    private final Sort.Direction direction;

    ComplaintSort(String field, Sort.Direction direction) {
        this.field = field;
        this.direction = direction;
    }
}
