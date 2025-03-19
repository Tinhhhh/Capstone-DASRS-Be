package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.Review;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.repository.ReviewRepository;
import com.sep490.dasrsbackend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public Review reviewRecord(Long reviewId, String reply, ReviewStatus status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Review not found"));

        review.setReply(reply);
        review.setStatus(status);
        review.setLastModifiedDate(new Date());

        return reviewRepository.save(review); // Save the updated review using the repository
    }
    @Override
    public List<Review> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        if (reviews.isEmpty()) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "No reviews found");
        }
        return reviews;
    }

    @Override
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    @Override
    public List<Review> getReviewsByMatchId(Long matchId) {
        List<Review> reviews = reviewRepository.findByMatchId(matchId);
        if (reviews.isEmpty()) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "No reviews found for the given match ID");
        }
        return reviews;
    }
    @Override
    public Review updateReviewStatus(Long reviewId, ReviewStatus status) {
        Review review = getReviewById(reviewId);

        // Update the status
        review.setStatus(status);
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long reviewId) {
        Review review = getReviewById(reviewId);
        reviewRepository.delete(review);
    }
}

