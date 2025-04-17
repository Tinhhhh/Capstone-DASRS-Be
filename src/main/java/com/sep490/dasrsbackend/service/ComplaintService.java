package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.entity.Complaint;
import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import com.sep490.dasrsbackend.model.payload.request.CreateReviewRequest;
import com.sep490.dasrsbackend.model.payload.request.ReplyReviewRequest;
import com.sep490.dasrsbackend.model.payload.response.ComplaintResponse;

import java.util.List;
import java.util.UUID;

public interface ComplaintService {
    Complaint reviewRecord(Long reviewId, String reply, ComplaintStatus status);

    List<ComplaintResponse> getAllReviews();

    ComplaintResponse getReviewById(Long reviewId);

    List<ComplaintResponse> getReviewsByMatchId(Long matchId);

    ComplaintResponse updateReviewStatus(Long reviewId, ComplaintStatus status);

    void deleteReview(Long reviewId);

    ComplaintResponse createReview(UUID accountId, Long matchId, CreateReviewRequest createReviewRequest);

    ComplaintResponse replyReview(Long id, ReplyReviewRequest replyReviewRequest);
}
