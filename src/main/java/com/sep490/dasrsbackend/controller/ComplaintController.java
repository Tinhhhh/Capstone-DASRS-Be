package com.sep490.dasrsbackend.controller;

import com.sep490.dasrsbackend.Util.AppConstants;
import com.sep490.dasrsbackend.model.entity.Complaint;
import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import com.sep490.dasrsbackend.model.exception.ResponseBuilder;
import com.sep490.dasrsbackend.model.payload.request.*;
import com.sep490.dasrsbackend.model.payload.response.ComplaintResponse;
import com.sep490.dasrsbackend.model.payload.response.ComplaintResponseDetails;
import com.sep490.dasrsbackend.model.payload.response.PaginatedComplaintResponse;
import com.sep490.dasrsbackend.model.payload.response.RoundComplaintResponse;
import com.sep490.dasrsbackend.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaint", description = "APIs for managing reviews")
public class ComplaintController {

    private final ComplaintService conplaintService;
    private final ComplaintService complaintService;

//    @Operation(summary = "Update a review record", description = "Update the review details by ID.")
//    @PostMapping("/review")
//    public ResponseEntity<Object> reviewRecord(
//            @RequestParam Long reviewId,
//            @RequestParam String reply,
//            @RequestParam ComplaintStatus status) {
//        Complaint complaint = conplaintService.reviewRecord(reviewId, reply, status);
//        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaint updated successfully.", complaint);
//    }
//
//    @Operation(summary = "Get all reviews", description = "Retrieve a list of all reviews.")
//    @GetMapping
//    public ResponseEntity<Object> getAllReviews() {
//        List<ComplaintResponse> reviews = conplaintService.getAllReviews();
//        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Reviews retrieved successfully.", reviews);
//    }
//
//    @Operation(summary = "Get review by ID", description = "Retrieve a specific review by its ID.")
//    @GetMapping("/{id}")
//    public ResponseEntity<Object> getReviewById(@PathVariable Long id) {
//        ComplaintResponse review = conplaintService.getReviewById(id);
//        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaint retrieved successfully.", review);
//    }
//
//    @Operation(summary = "Get reviews by Match ID", description = "Retrieve reviews for a specific match.")
//    @GetMapping("/match/{matchId}")
//    public ResponseEntity<Object> getReviewsByMatchId(@PathVariable Long matchId) {
//        List<ComplaintResponse> reviews = conplaintService.getReviewsByMatchId(matchId);
//        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Reviews retrieved successfully.", reviews);
//    }
//
//    @Operation(summary = "Update review status", description = "Change the status of a review by its ID.")
//    @PatchMapping("/update-status")
//    public ResponseEntity<Object> updateReviewStatus(@RequestParam Long reviewId, @RequestParam ComplaintStatus status) {
//        ComplaintResponse updatedReview = conplaintService.updateReviewStatus(reviewId, status);
//        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaint status updated successfully.", updatedReview);
//    }
//
//    @Operation(summary = "Delete a review", description = "Remove a review by its ID.")
//    @DeleteMapping("/delete")
//    public ResponseEntity<Object> deleteReview(@RequestParam Long reviewId) {
//        conplaintService.deleteReview(reviewId);
//        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Complaint deleted successfully.");
//    }

//    @Operation(summary = "Create a new review", description = "Submit a new review.")
//    @PostMapping
//    public ResponseEntity<Object> createReview(
//            @RequestParam("uuid") UUID accountUuid,
//            @RequestParam("matchId") Long matchId,
//            @Validated @RequestBody CreateReviewRequest createReviewRequest) {
//
//        ComplaintResponse response = conplaintService.createReview(accountUuid, matchId, createReviewRequest);
//        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaint created successfully.", response);
//    }
//
//    @Operation(summary = "Reply to a review", description = "Provide a reply to a specific review.")
//    @PutMapping("/reply/{id}")
//    public ResponseEntity<Object> replyReview(
//            @PathVariable Long id,
//            @Validated @RequestBody ReplyReviewRequest replyReviewRequest) {
//
//        ComplaintResponse response = conplaintService.replyReview(id, replyReviewRequest);
//        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Reply added successfully.", response);
//    }

    @Operation(summary = "Create a complaint", description = "Create a new complaint by providing match, team, and account IDs along with complaint details.")
    @PostMapping("/create")
    public ResponseEntity<Object> createComplaint(
            @RequestParam Long matchTeamId,
            @RequestBody ComplaintRequest request) {

        ComplaintResponseDetails response = complaintService.createComplaint(matchTeamId, request);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.CREATED, "Complaint created successfully", response);
    }

    @Operation(summary = "Reply to a complaint", description = "Reply to a complaint with a status update and the reply content.")
    @PutMapping("/reply/{id}")
    public ResponseEntity<Object> replyToComplaint(
            @PathVariable Long id,
            @RequestBody ComplaintReplyRequest replyRequest) {

        ComplaintResponseDetails response = complaintService.replyToComplaint(id, replyRequest);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaint replied successfully", response);
    }

    @Operation(summary = "Get a complaint by ID", description = "Fetch the complaint details by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getComplaintById(@PathVariable Long id) {
        ComplaintResponseDetails response = complaintService.getComplaintById(id);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaint fetched successfully", response);
    }

    @Operation(summary = "Get all complaints grouped by rounds", description = "Fetch all complaints grouped by their respective rounds")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        List<RoundComplaintResponse> responses = complaintService.getAllComplaints(status, sortBy, sortDirection);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaints grouped by rounds fetched successfully", responses);
    }

    @Operation(summary = "Delete a complaint by ID", description = "Delete the complaint by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseBuilder.responseBuilder(HttpStatus.NO_CONTENT, "Complaint deleted successfully");
    }

    @Operation(summary = "Get complaints by status", description = "Fetch complaints filtered by their status")
    @GetMapping("/status/{status}")
    public ResponseEntity<Object> getComplaintsByStatus(@PathVariable ComplaintStatus status) {
        List<ComplaintResponseDetails> responses = complaintService.getComplaintsByStatus(status);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaints fetched successfully", responses);
    }

    @Operation(summary = "Get complaints by match ID", description = "Fetch all complaints related to a specific match")
    @GetMapping("/match/{matchId}")
    public ResponseEntity<Object> getComplaintsByMatchId(@PathVariable Long matchId) {
        List<ComplaintResponseDetails> responses = complaintService.getComplaintsByMatchId(matchId);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaints fetched successfully", responses);
    }

    @Operation(summary = "Get complaints by team ID", description = "Fetch all complaints related to a specific team")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<Object> getComplaintsByTeamId(@PathVariable Long teamId) {
        List<ComplaintResponseDetails> responses = complaintService.getComplaintsByTeamId(teamId);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaints fetched successfully", responses);
    }

    @Operation(summary = "Update a complaint", description = "Allows players to update the title or description of an existing complaint.")
    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateComplaint(
            @PathVariable Long id,
            @RequestBody ComplaintUpdateRequest updateRequest) {
        ComplaintResponseDetails updatedComplaint = complaintService.updateComplaint(id, updateRequest);
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaint updated successfully", updatedComplaint);
    }

    @Operation(summary = "Get complaints by round ID", description = "Retrieve complaints related to a specific round with pagination, sorting, and optional filtering by status.")
    @GetMapping("/round/{roundId}")
    public ResponseEntity<Object> getComplaintsByRoundId(
            @PathVariable Long roundId,
            @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDirection,
            @RequestParam(name = "status", required = false) ComplaintStatus status
    ) {
            PaginatedComplaintResponse response = complaintService.getComplaintsByRoundId(roundId, status, pageNo, pageSize, sortBy, sortDirection);
            return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Complaints retrieved successfully.", response);
        }
}
