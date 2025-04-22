package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.entity.Complaint;
import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import com.sep490.dasrsbackend.model.payload.request.*;
import com.sep490.dasrsbackend.model.payload.response.ComplaintResponse;
import com.sep490.dasrsbackend.model.payload.response.ComplaintResponseDetails;
import com.sep490.dasrsbackend.model.payload.response.PaginatedComplaintResponse;
import com.sep490.dasrsbackend.model.payload.response.RoundComplaintResponse;

import java.util.List;
import java.util.UUID;

public interface ComplaintService {
//    Complaint reviewRecord(Long reviewId, String reply, ComplaintStatus status);
//
//    List<ComplaintResponse> getAllReviews();
//
//    ComplaintResponse getReviewById(Long reviewId);
//
//    List<ComplaintResponse> getReviewsByMatchId(Long matchId);
//
//    ComplaintResponse updateReviewStatus(Long reviewId, ComplaintStatus status);
//
//    void deleteReview(Long reviewId);
//
//    ComplaintResponse createReview(UUID accountId, Long matchId, CreateReviewRequest createReviewRequest);
//
//    ComplaintResponse replyReview(Long id, ReplyReviewRequest replyReviewRequest);

    ComplaintResponseDetails createComplaint(Long matchTeamId, ComplaintRequest request);

    ComplaintResponseDetails replyToComplaint(Long id, ComplaintReplyRequest replyRequest);

    ComplaintResponseDetails getComplaintById(Long id);

    List<RoundComplaintResponse> getAllComplaints(ComplaintStatus status, String sortBy, String sortDirection);

    void deleteComplaint(Long id);

    List<ComplaintResponseDetails> getComplaintsByRoundIdAndStatus(Long roundId, ComplaintStatus status);

    List<ComplaintResponseDetails> getComplaintsByMatchId(Long matchId);

    List<ComplaintResponseDetails> getComplaintsByTeamId(Long teamId);

    ComplaintResponseDetails updateComplaint(Long id, ComplaintUpdateRequest updateRequest);

    PaginatedComplaintResponse getComplaintsByRoundId(Long roundId, ComplaintStatus status, int page, int size, String sortBy, String sortDirection);
}
