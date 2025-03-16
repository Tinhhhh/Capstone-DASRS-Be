package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.model.entity.Review;
import com.sep490.dasrsbackend.model.enums.ReviewStatus;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
