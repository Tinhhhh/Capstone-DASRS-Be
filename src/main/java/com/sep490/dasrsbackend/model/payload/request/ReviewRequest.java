package com.sep490.dasrsbackend.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @JsonProperty("title")
    @NotBlank(message = "Title is required")
    private String title;

    @JsonProperty("description")
    @NotBlank(message = "Description is required")
    private String description;

    @JsonProperty("reply")
    private String reply;

    @JsonProperty("review_status")
    @NotNull(message = "Review status is required")
    private ReviewStatus status;

    @JsonProperty("match_id")
    @NotNull(message = "Match ID is required")
    private Long matchId;
}
