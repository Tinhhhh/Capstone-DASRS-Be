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
}

