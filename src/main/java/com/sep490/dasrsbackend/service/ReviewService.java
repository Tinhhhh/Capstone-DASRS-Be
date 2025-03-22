package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.entity.Review;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;

import java.util.List;

public interface ReviewService {
    Review reviewRecord(Long reviewId, String reply, ReviewStatus status);
    List<Review> getAllReviews();
    Review getReviewById(Long reviewId);
    List<Review> getReviewsByMatchId(Long matchId);
    Review updateReviewStatus(Long reviewId, ReviewStatus status);
    void deleteReview(Long reviewId);
}
