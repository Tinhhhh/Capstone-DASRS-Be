package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.entity.Review;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Review a record", description = "Review and reply to a submitted record.")
    @PostMapping
    public ResponseEntity<Object> reviewRecord(
            @RequestParam Long reviewId,
            @RequestParam String reply,
            @RequestParam ReviewStatus status) {
        Review review = reviewService.reviewRecord(reviewId, reply, status);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Review updated successfully.", review);
    }
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/by-id")
    public ResponseEntity<Review> getReviewById(@RequestParam Long reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/by-match")
    public ResponseEntity<List<Review>> getReviewsByMatchId(@RequestParam Long matchId) {
        List<Review> reviews = reviewService.getReviewsByMatchId(matchId);
        return ResponseEntity.ok(reviews);
    }
    @PatchMapping("/update-status")
    public ResponseEntity<Review> updateReviewStatus(@RequestParam Long reviewId, @RequestParam ReviewStatus status) {
        Review updatedReview = reviewService.updateReviewStatus(reviewId, status);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteReview(@RequestParam Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok("Review deleted successfully");
    }
}
