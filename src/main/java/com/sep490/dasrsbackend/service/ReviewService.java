package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.entity.Review;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;

public interface ReviewService {
    Review reviewRecord(Long reviewId, String reply, ReviewStatus status);
}
