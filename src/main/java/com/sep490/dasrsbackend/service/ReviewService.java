package com.sep490.dasrsbackend.service;


import com.sep490.dasrsbackend.model.payload.request.ReviewRequest;
import com.sep490.dasrsbackend.model.payload.response.ReviewResponse;
import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request);
    ReviewResponse updateReview(Long id, ReviewRequest request);
    ReviewResponse getReviewById(Long id);
    void deleteReview(Long id);
    List<ReviewResponse> getAllReviews();
}
