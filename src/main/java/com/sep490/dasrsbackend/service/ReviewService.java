package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.entity.Review;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;
import com.sep490.dasrsbackend.model.payload.request.CreateReviewRequest;
import com.sep490.dasrsbackend.model.payload.request.ReplyReviewRequest;
import com.sep490.dasrsbackend.model.payload.response.ReviewResponse;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    Review reviewRecord(Long reviewId, String reply, ReviewStatus status);

    List<Review> getAllReviews();

    Review getReviewById(Long reviewId);

    List<Review> getReviewsByMatchId(Long matchId);

    Review updateReviewStatus(Long reviewId, ReviewStatus status);

    void deleteReview(Long reviewId);

    ReviewResponse createReview(UUID accountId, Long matchId, CreateReviewRequest createReviewRequest);

    ReviewResponse replyReview(ReplyReviewRequest replyReviewRequest);
}
