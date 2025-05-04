package com.sep490.dasrsbackend.model.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnityRoomRequest {

    private UUID accountId;

    private String matchCode;

    private Date joinTime;

}
