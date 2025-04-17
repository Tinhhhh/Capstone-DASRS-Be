package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.Complaint;
import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.CreateReviewRequest;
import com.sep490.dasrsbackend.model.payload.request.ReplyReviewRequest;
import com.sep490.dasrsbackend.model.payload.response.ComplaintResponse;
import com.sep490.dasrsbackend.repository.AccountRepository;
import com.sep490.dasrsbackend.repository.MatchRepository;
import com.sep490.dasrsbackend.repository.ComplaintRepository;
import com.sep490.dasrsbackend.service.ComplaintService;
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
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final MatchRepository matchRepository;
    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;

    @Override
    public Complaint reviewRecord(Long reviewId, String reply, ComplaintStatus status) {
        Complaint complaint = complaintRepository.findById(reviewId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Complaint not found"));

        complaint.setReply(reply);
        complaint.setStatus(status);
        complaint.setLastModifiedDate(new Date());

        return complaintRepository.save(complaint);
    }

    @Override
    public List<ComplaintResponse> getAllReviews() {
        List<Complaint> complaints = complaintRepository.findAll();
        if (complaints.isEmpty()) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "No complaints found");
        }
        return complaints.stream().map(this::mapToResponse).toList();
    }

    @Override
    public ComplaintResponse getReviewById(Long reviewId) {
        Complaint complaint = complaintRepository.findById(reviewId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Complaint not found"));
        return mapToResponse(complaint);
    }

    @Override
    public List<ComplaintResponse> getReviewsByMatchId(Long matchId) {
        List<Complaint> complaints = complaintRepository.findByMatchId(matchId);
        if (complaints.isEmpty()) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "No complaints found for the given match ID");
        }
        return complaints.stream().map(this::mapToResponse).toList();
    }

    @Override
    public ComplaintResponse updateReviewStatus(Long reviewId, ComplaintStatus status) {
        Complaint complaint = complaintRepository.findById(reviewId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Complaint not found"));

        complaint.setStatus(status);
        complaint = complaintRepository.save(complaint);

        return mapToResponse(complaint);
    }

    @Override
    public void deleteReview(Long reviewId) {
        Complaint complaint = complaintRepository.findById(reviewId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Complaint not found"));
        complaintRepository.delete(complaint);
    }

    @Override
    public ComplaintResponse createReview(UUID accountId, Long matchId, CreateReviewRequest createReviewRequest) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found for UUID: " + accountId));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found for ID: " + matchId));

        Complaint complaint = Complaint.builder()
                .title(createReviewRequest.getTitle())
                .description(createReviewRequest.getDescription())
                .status(ComplaintStatus.PENDING)
                .match(match)
                .build();

        complaint = complaintRepository.save(complaint);
        return modelMapper.map(complaint, ComplaintResponse.class);
    }

    @Override
    public ComplaintResponse replyReview(Long id, ReplyReviewRequest replyReviewRequest) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with ID: " + id));

        complaint.setReply(replyReviewRequest.getReply());
        complaint.setStatus(replyReviewRequest.getStatus());
        complaint.setLastModifiedDate(new Date());

        complaint = complaintRepository.save(complaint);
        return modelMapper.map(complaint, ComplaintResponse.class);
    }

    private ComplaintResponse mapToResponse(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .reply(complaint.getReply())
                .status(complaint.getStatus())
                .createdDate(complaint.getCreatedDate())
                .lastModifiedDate(complaint.getLastModifiedDate())
                .matchId(complaint.getMatch().getId())
                .build();
    }
}

