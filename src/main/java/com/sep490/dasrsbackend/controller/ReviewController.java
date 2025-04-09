package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.entity.Review;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.CreateReviewRequest;
import com.sep490.dasrsbackend.model.payload.request.ReplyReviewRequest;
import com.sep490.dasrsbackend.model.payload.response.ReviewResponse;
import com.sep490.dasrsbackend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "APIs for managing reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Update a review record", description = "Update the review details by ID.")
    @PostMapping("/review")
    public ResponseEntity<Object> reviewRecord(
            @RequestParam Long reviewId,
            @RequestParam String reply,
            @RequestParam ReviewStatus status) {
        Review review = reviewService.reviewRecord(reviewId, reply, status);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Review updated successfully.", review);
    }

    @Operation(summary = "Retrieve all reviews", description = "Fetch all available reviews.")
    @GetMapping
    public ResponseEntity<Object> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "All reviews fetched successfully.", reviews);
    }

    @Operation(summary = "Retrieve a review by ID", description = "Fetch a review using its ID.")
    @GetMapping("/by-id")
    public ResponseEntity<Object> getReviewById(@RequestParam Long reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Review fetched successfully.", review);
    }

    @Operation(summary = "Retrieve reviews by match ID", description = "Fetch reviews associated with a specific match.")
    @GetMapping("/by-match")
    public ResponseEntity<Object> getReviewsByMatchId(@RequestParam Long matchId) {
        List<Review> reviews = reviewService.getReviewsByMatchId(matchId);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Reviews for the match fetched successfully.", reviews);
    }

    @Operation(summary = "Update review status", description = "Change the status of a review by its ID.")
    @PatchMapping("/update-status")
    public ResponseEntity<Object> updateReviewStatus(@RequestParam Long reviewId, @RequestParam ReviewStatus status) {
        Review updatedReview = reviewService.updateReviewStatus(reviewId, status);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Review status updated successfully.", updatedReview);
    }

    @Operation(summary = "Delete a review", description = "Remove a review by its ID.")
    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteReview(@RequestParam Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Review deleted successfully.");
    }

    @Operation(summary = "Create a new review", description = "Submit a new review.")
    @PostMapping
    public ResponseEntity<Object> createReview(
            @RequestParam("uuid") UUID accountUuid,
            @RequestParam("matchId") Long matchId,
            @Validated @RequestBody CreateReviewRequest createReviewRequest) {

        ReviewResponse response = reviewService.createReview(accountUuid, matchId, createReviewRequest);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Review created successfully.", response);
    }

    @Operation(summary = "Reply to a review", description = "Provide a reply to a specific review.")
    @PutMapping("/reply/{id}")
    public ResponseEntity<Object> replyReview(
            @PathVariable Long id,
            @Validated @RequestBody ReplyReviewRequest replyReviewRequest) {

        replyReviewRequest.setReviewId(id);
        ReviewResponse response = reviewService.replyReview(replyReviewRequest);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Reply added successfully.", response);
    }
}
