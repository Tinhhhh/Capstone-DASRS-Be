package com.sep490.dasrsbackend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyDashboard {

    @JsonProperty("week")
    private int week;

    @JsonProperty("weekly_dashboard")
    private List<TodayDashboard> weeklyDashboard;
}
