package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.entity.Review;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.ResourceNotFoundException;
import com.sep490.dasrsbackend.model.payload.request.CreateReviewRequest;
import com.sep490.dasrsbackend.model.payload.request.ReplyReviewRequest;
import com.sep490.dasrsbackend.model.payload.response.ReviewResponse;
import com.sep490.dasrsbackend.repository.AccountRepository;
import com.sep490.dasrsbackend.repository.MatchRepository;
import com.sep490.dasrsbackend.repository.ReviewRepository;
import com.sep490.dasrsbackend.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MatchRepository matchRepository;
    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;

    @Override
    public Review reviewRecord(Long reviewId, String reply, ReviewStatus status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Review not found"));

        review.setReply(reply);
        review.setStatus(status);
        review.setLastModifiedDate(new Date());

        return reviewRepository.save(review);
    }

    @Override
    public List<ReviewResponse> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        if (reviews.isEmpty()) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "No reviews found");
        }
        return reviews.stream().map(this::mapToResponse).toList();
    }

    @Override
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Review not found"));
        return mapToResponse(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByMatchId(Long matchId) {
        List<Review> reviews = reviewRepository.findByMatchId(matchId);
        if (reviews.isEmpty()) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "No reviews found for the given match ID");
        }
        return reviews.stream().map(this::mapToResponse).toList();
    }

    @Override
    public ReviewResponse updateReviewStatus(Long reviewId, ReviewStatus status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Review not found"));

        review.setStatus(status);
        review = reviewRepository.save(review);

        return mapToResponse(review);
    }

    @Override
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Review not found"));
        reviewRepository.delete(review);
    }

    @Override
    public ReviewResponse createReview(UUID accountId, Long matchId, CreateReviewRequest createReviewRequest) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found for UUID: " + accountId));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found for ID: " + matchId));

        Review review = Review.builder()
                .title(createReviewRequest.getTitle())
                .description(createReviewRequest.getDescription())
                .status(ReviewStatus.PENDING)
                .match(match)
                .build();

        review = reviewRepository.save(review);
        return modelMapper.map(review, ReviewResponse.class);
    }

    @Override
    public ReviewResponse replyReview(Long id, ReplyReviewRequest replyReviewRequest) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with ID: " + id));

        review.setReply(replyReviewRequest.getReply());
        review.setStatus(replyReviewRequest.getStatus());
        review.setLastModifiedDate(new Date());

        review = reviewRepository.save(review);
        return modelMapper.map(review, ReviewResponse.class);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .title(review.getTitle())
                .description(review.getDescription())
                .reply(review.getReply())
                .status(review.getStatus())
                .createdDate(review.getCreatedDate())
                .lastModifiedDate(review.getLastModifiedDate())
                .matchId(review.getMatch().getId())
                .build();
    }
}

