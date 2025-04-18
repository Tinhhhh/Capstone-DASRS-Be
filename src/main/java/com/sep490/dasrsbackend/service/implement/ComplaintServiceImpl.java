package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.ResourceNotFoundException;
import com.sep490.dasrsbackend.model.payload.request.*;
import com.sep490.dasrsbackend.model.payload.response.ComplaintResponse;
import com.sep490.dasrsbackend.model.payload.response.ComplaintResponseDetails;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.ComplaintService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final MatchRepository matchRepository;
    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final TeamRepository teamRepository;

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

    @Override
    public ComplaintResponseDetails createComplaint(Long matchId, Long teamId, UUID accountId, ComplaintRequest request) {
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description must not be empty");
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Complaint complaint = Complaint.builder()
                .match(match)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(ComplaintStatus.PENDING)
                .createdDate(new Date())
                .lastModifiedDate(new Date())
                .build();

        complaint = complaintRepository.save(complaint);

        return mapToResponseDetails(complaint, team, account);
    }

    @Override
    public ComplaintResponseDetails replyToComplaint(Long id, ComplaintReplyRequest replyRequest) {
        // Validate the reply and status
        if (replyRequest.getReply() == null || replyRequest.getReply().isEmpty()) {
            throw new IllegalArgumentException("Reply must not be empty");
        }
        if (replyRequest.getStatus() == null) {
            throw new IllegalArgumentException("Status must not be null");
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        complaint.setReply(replyRequest.getReply());
        complaint.setStatus(replyRequest.getStatus());
        complaint.setLastModifiedDate(new Date());

        complaint = complaintRepository.save(complaint);

        Match match = complaint.getMatch();
        List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());
        MatchTeam matchTeam = matchTeams.stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No MatchTeam found for matchId: " + match.getId()));

        return mapToResponseDetails(complaint, matchTeam.getTeam(), matchTeam.getAccount());
    }

    private ComplaintResponseDetails mapToResponseDetails(Complaint complaint, Team team, Account account) {
        Long matchId = complaint.getMatch().getId();

        List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(matchId);
        if (matchTeams.isEmpty()) {
            throw new ResourceNotFoundException("No MatchTeam found for matchId: " + matchId);
        }

        return ComplaintResponseDetails.builder()
                .id(complaint.getId())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .reply(complaint.getReply())
                .status(complaint.getStatus())
                .createdDate(complaint.getCreatedDate())
                .lastModifiedDate(complaint.getLastModifiedDate())
                .matchId(matchId)
                .teamId(team.getId())
                .accountId(account.getAccountId())
                .build();
    }

    @Override
    public ComplaintResponseDetails getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        Match match = complaint.getMatch();
        List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());
        MatchTeam matchTeam = matchTeams.stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No MatchTeam found for matchId: " + match.getId()));

        return mapToResponseDetails(complaint, matchTeam.getTeam(), matchTeam.getAccount());
    }

    @Override
    public List<ComplaintResponseDetails> getAllComplaints() {
        List<Complaint> complaints = complaintRepository.findAll();

        if (complaints.isEmpty()) {
            throw new ResourceNotFoundException("No complaints found");
        }

        return complaints.stream()
                .map(complaint -> {
                    Match match = complaint.getMatch();
                    List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());
                    MatchTeam matchTeam = matchTeams.stream().findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("No MatchTeam found for matchId: " + match.getId()));
                    return mapToResponseDetails(complaint, matchTeam.getTeam(), matchTeam.getAccount());
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteComplaint(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        complaintRepository.delete(complaint);
    }

    @Override
    public List<ComplaintResponseDetails> getComplaintsByStatus(ComplaintStatus status) {
        List<Complaint> complaints = complaintRepository.findByStatus(status);

        if (complaints.isEmpty()) {
            throw new ResourceNotFoundException("No complaints found with status: " + status);
        }

        return complaints.stream()
                .map(complaint -> {
                    Match match = complaint.getMatch();
                    List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());
                    MatchTeam matchTeam = matchTeams.stream().findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("No MatchTeam found for matchId: " + match.getId()));
                    return mapToResponseDetails(complaint, matchTeam.getTeam(), matchTeam.getAccount());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplaintResponseDetails> getComplaintsByMatchId(Long matchId) {
        List<Complaint> complaints = complaintRepository.findByMatchId(matchId);

        if (complaints.isEmpty()) {
            throw new ResourceNotFoundException("No complaints found for matchId: " + matchId);
        }

        return complaints.stream()
                .map(complaint -> {
                    Match match = complaint.getMatch();
                    List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());
                    MatchTeam matchTeam = matchTeams.stream().findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("No MatchTeam found for matchId: " + match.getId()));
                    return mapToResponseDetails(complaint, matchTeam.getTeam(), matchTeam.getAccount());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplaintResponseDetails> getComplaintsByTeamId(Long teamId) {
        List<Complaint> complaints = complaintRepository.findByMatch_MatchTeamList_Team_Id(teamId);

        if (complaints.isEmpty()) {
            throw new ResourceNotFoundException("No complaints found for teamId: " + teamId);
        }

        return complaints.stream()
                .map(complaint -> {
                    Match match = complaint.getMatch();
                    List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());
                    MatchTeam matchTeam = matchTeams.stream().findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("No MatchTeam found for matchId: " + match.getId()));
                    return mapToResponseDetails(complaint, matchTeam.getTeam(), matchTeam.getAccount());
                })
                .collect(Collectors.toList());
    }

    @Override
    public ComplaintResponseDetails updateComplaint(Long id, ComplaintUpdateRequest updateRequest) {
        if (updateRequest.getTitle() == null || updateRequest.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        if (updateRequest.getDescription() == null || updateRequest.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description must not be empty");
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        complaint.setTitle(updateRequest.getTitle());
        complaint.setDescription(updateRequest.getDescription());
        complaint.setLastModifiedDate(new Date());

        complaint = complaintRepository.save(complaint);

        Team team = complaint.getMatch().getMatchTeamList().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"))
                .getTeam();

        Account account = complaint.getMatch().getMatchTeamList().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"))
                .getAccount();

        return mapToResponseDetails(complaint, team, account);
    }

}


