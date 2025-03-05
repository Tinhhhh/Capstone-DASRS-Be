package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.converter.ReviewConverter;
import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.entity.Review;
import com.sep490.dasrsbackend.model.payload.request.ReviewRequest;
import com.sep490.dasrsbackend.model.payload.response.ReviewResponse;
import com.sep490.dasrsbackend.repository.MatchRepository;
import com.sep490.dasrsbackend.repository.ReviewRepository;
import com.sep490.dasrsbackend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ReviewConverter reviewConverter;

    @Override
    public ReviewResponse createReview(ReviewRequest request) {
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        Review review = reviewConverter.toEntity(request, match);
        return reviewConverter.toResponse(reviewRepository.save(review));
    }

    @Override
    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        existingReview.setTitle(request.getTitle());
        existingReview.setDescription(request.getDescription());
        existingReview.setReply(request.getReply());
        existingReview.setStatus(request.getStatus());
        existingReview.setMatch(match);
        return reviewConverter.toResponse(reviewRepository.save(existingReview));
    }

    @Override
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        return reviewConverter.toResponse(review);
    }

    @Override
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    @Override
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewConverter::toResponse)
                .collect(Collectors.toList());
    }
}
