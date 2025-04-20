package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.ComplaintSpecification;
import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.ComplaintStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.ResourceNotFoundException;
import com.sep490.dasrsbackend.model.payload.request.*;
import com.sep490.dasrsbackend.model.payload.response.ComplaintResponse;
import com.sep490.dasrsbackend.model.payload.response.ComplaintResponseDetails;
import com.sep490.dasrsbackend.model.payload.response.ComplaintResponseWithDetails;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.ComplaintService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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

//    @Override
//    public Complaint reviewRecord(Long reviewId, String reply, ComplaintStatus status) {
//        Complaint complaint = complaintRepository.findById(reviewId)
//                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Complaint not found"));
//
//        complaint.setReply(reply);
//        complaint.setStatus(status);
//        complaint.setLastModifiedDate(new Date());
//
//        return complaintRepository.save(complaint);
//    }
//
//    @Override
//    public List<ComplaintResponse> getAllReviews() {
//        List<Complaint> complaints = complaintRepository.findAll();
//        if (complaints.isEmpty()) {
//            throw new DasrsException(HttpStatus.NOT_FOUND, "No complaints found");
//        }
//        return complaints.stream().map(this::mapToResponse).toList();
//    }
//
//    @Override
//    public ComplaintResponse getReviewById(Long reviewId) {
//        Complaint complaint = complaintRepository.findById(reviewId)
//                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Complaint not found"));
//        return mapToResponse(complaint);
//    }
//
//    @Override
//    public List<ComplaintResponse> getReviewsByMatchId(Long matchId) {
//        List<Complaint> complaints = complaintRepository.findByMatchId(matchId);
//        if (complaints.isEmpty()) {
//            throw new DasrsException(HttpStatus.NOT_FOUND, "No complaints found for the given match ID");
//        }
//        return complaints.stream().map(this::mapToResponse).toList();
//    }
//
//    @Override
//    public ComplaintResponse updateReviewStatus(Long reviewId, ComplaintStatus status) {
//        Complaint complaint = complaintRepository.findById(reviewId)
//                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Complaint not found"));
//
//        complaint.setStatus(status);
//        complaint = complaintRepository.save(complaint);
//
//        return mapToResponse(complaint);
//    }
//
//    @Override
//    public void deleteReview(Long reviewId) {
//        Complaint complaint = complaintRepository.findById(reviewId)
//                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Complaint not found"));
//        complaintRepository.delete(complaint);
//    }
//
//    @Override
//    public ComplaintResponse createReview(UUID accountId, Long matchId, CreateReviewRequest createReviewRequest) {
//        accountRepository.findById(accountId)
//                .orElseThrow(() -> new EntityNotFoundException("Account not found for UUID: " + accountId));
//
//        Match match = matchRepository.findById(matchId)
//                .orElseThrow(() -> new EntityNotFoundException("Match not found for ID: " + matchId));
//
//        Complaint complaint = Complaint.builder()
//                .title(createReviewRequest.getTitle())
//                .description(createReviewRequest.getDescription())
//                .status(ComplaintStatus.PENDING)
//                .match(match)
//                .build();
//
//        complaint = complaintRepository.save(complaint);
//        return modelMapper.map(complaint, ComplaintResponse.class);
//    }
//
//    @Override
//    public ComplaintResponse replyReview(Long id, ReplyReviewRequest replyReviewRequest) {
//        Complaint complaint = complaintRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with ID: " + id));
//
//        complaint.setReply(replyReviewRequest.getReply());
//        complaint.setStatus(replyReviewRequest.getStatus());
//        complaint.setLastModifiedDate(new Date());
//
//        complaint = complaintRepository.save(complaint);
//        return modelMapper.map(complaint, ComplaintResponse.class);
//    }
//
//    private ComplaintResponse mapToResponse(Complaint complaint) {
//        return ComplaintResponse.builder()
//                .id(complaint.getId())
//                .title(complaint.getTitle())
//                .description(complaint.getDescription())
//                .reply(complaint.getReply())
//                .status(complaint.getStatus())
//                .createdDate(complaint.getCreatedDate())
//                .lastModifiedDate(complaint.getLastModifiedDate())
//                .matchId(complaint.getMatch().getId())
//                .build();
//    }

    @Override
    public ComplaintResponseDetails createComplaint(Long matchTeamId, ComplaintRequest request) {
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description must not be empty");
        }

        MatchTeam matchTeam = matchTeamRepository.findById(matchTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchTeam not found for ID: " + matchTeamId));

        if (matchTeam.getTeam() == null) {
            throw new IllegalArgumentException("MatchTeam is missing an associated Team for ID: " + matchTeamId);
        }
        if (matchTeam.getMatch() == null) {
            throw new IllegalArgumentException("MatchTeam is missing an associated Match for ID: " + matchTeamId);
        }
        if (matchTeam.getAccount() == null) {
            throw new IllegalArgumentException("MatchTeam is missing an associated Account for ID: " + matchTeamId);
        }

        Complaint complaint = Complaint.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(ComplaintStatus.PENDING)
                .matchTeam(matchTeam)
                .build();

        complaint = complaintRepository.save(complaint);

        return mapToResponseDetails(matchTeamId, complaint);
    }


    private ComplaintResponseDetails mapToResponseDetails(Long matchTeamId, Complaint complaint) {
        MatchTeam matchTeam = matchTeamRepository.findById(matchTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchTeam not found for ID: " + matchTeamId));

        Match match = matchTeam.getMatch();
        Team team = matchTeam.getTeam();
        Account account = matchTeam.getAccount();

        Date createdDate = complaint.getCreatedDate();
        Date lastModifiedDate = complaint.getLastModifiedDate();

        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        String formattedCreatedDate = sdf.format(createdDate);
        String formattedLastModifiedDate = sdf.format(lastModifiedDate);

        return ComplaintResponseDetails.builder()
                .id(complaint.getId())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .reply(complaint.getReply())
                .status(complaint.getStatus())
                .createdDate(formattedCreatedDate)
                .lastModifiedDate(formattedLastModifiedDate)
                .matchId(match.getId())
                .teamId(team.getId())
                .accountId(account.getAccountId())
                .matchTeamId(matchTeam.getId())
                .matchName(match.getMatchName())
                .teamName(team.getTeamName())
                .build();
    }

    @Override
    public ComplaintResponseDetails replyToComplaint(Long id, ComplaintReplyRequest replyRequest) {
        if (replyRequest.getReply() == null || replyRequest.getReply().isEmpty()) {
            throw new IllegalArgumentException("Reply must not be empty");
        }
        if (replyRequest.getStatus() == null) {
            throw new IllegalArgumentException("Status must not be null");
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        MatchTeam matchTeam = complaint.getMatchTeam();
        Match match = matchTeam.getMatch();
//        validateMatchAndTeams(matchTeam);

        complaint.setReply(replyRequest.getReply());
        complaint.setStatus(replyRequest.getStatus());
        complaint.setLastModifiedDate(new Date());

        complaint = complaintRepository.save(complaint);

        return mapToResponseDetails(matchTeam.getId(), complaint);
    }

    @Override
    public ComplaintResponseDetails getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        MatchTeam matchTeam = complaint.getMatchTeam();
        Match match = matchTeam.getMatch();
//        validateMatchAndTeams(matchTeam);

        return mapToResponseDetails(matchTeam.getId(), complaint);
    }

    @Override
    public List<ComplaintResponseDetails> getAllComplaints(ComplaintStatus status, String sortBy, String sortDirection) {
        Pageable pageable = getPageable(0, Integer.MAX_VALUE, sortBy, sortDirection);
        Specification<Complaint> spec = Specification.where(ComplaintSpecification.hasStatus(status));

        Page<Complaint> complaints = complaintRepository.findAll(spec, pageable);

        if (complaints.isEmpty()) {
            throw new ResourceNotFoundException("No complaints found");
        }

        return complaints.getContent().stream()
                .map(complaint -> {
                    MatchTeam matchTeam = complaint.getMatchTeam();
                    return mapToResponseDetails(matchTeam.getId(), complaint);
                })
                .collect(Collectors.toList());
    }

    private Pageable getPageable(int pageNo, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(pageNo, pageSize, sort);
    }

    @Override
    public void deleteComplaint(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

//        validateMatchAndTeams(complaint.getMatchTeam());

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
                    MatchTeam matchTeam = complaint.getMatchTeam();
                    Match match = matchTeam.getMatch();
//                    validateMatchAndTeams(matchTeam);
                    return mapToResponseDetails(matchTeam.getId(), complaint);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplaintResponseDetails> getComplaintsByMatchId(Long matchId) {
        List<Complaint> complaints = complaintRepository.findByMatchTeam_Match_Id(matchId);
        if (complaints.isEmpty()) {
            throw new ResourceNotFoundException("No complaints found for matchId: " + matchId);
        }

        return complaints.stream()
                .map(complaint -> {
                    MatchTeam matchTeam = complaint.getMatchTeam();
                    Match match = matchTeam.getMatch();
//                    validateMatchAndTeams(matchTeam);
                    return mapToResponseDetails(matchTeam.getId(), complaint);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplaintResponseDetails> getComplaintsByTeamId(Long teamId) {
        List<Complaint> complaints = complaintRepository.findByMatchTeam_Team_Id(teamId);
        if (complaints.isEmpty()) {
            throw new ResourceNotFoundException("No complaints found for teamId: " + teamId);
        }

        return complaints.stream()
                .map(complaint -> {
                    MatchTeam matchTeam = complaint.getMatchTeam();
                    Match match = matchTeam.getMatch();
//                    validateMatchAndTeams(matchTeam);
                    return mapToResponseDetails(matchTeam.getId(), complaint);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplaintResponseDetails> getComplaintsByRoundId(Long roundId) {
        if (roundId == null || roundId <= 0) {
            throw new IllegalArgumentException("Invalid roundId: " + roundId);
        }

        // Fetch matches by roundId
        List<Match> matches = matchRepository.findByRound_Id(roundId);
        if (matches.isEmpty()) {
            throw new ResourceNotFoundException("No matches found for roundId: " + roundId);
        }

        List<Long> matchIds = matches.stream()
                .map(Match::getId)
                .toList();

        List<Complaint> complaints = complaintRepository.findByRoundId(roundId);
        if (complaints.isEmpty()) {
            throw new ResourceNotFoundException("No complaints found for roundId: " + roundId);
        }

        return complaints.stream()
                .map(complaint -> {
                    MatchTeam matchTeam = complaint.getMatchTeam();
                    if (matchTeam == null) {
                        throw new IllegalStateException("Complaint ID " + complaint.getId() + " is not associated with any MatchTeam.");
                    }

                    return mapToResponseDetails(matchTeam.getId(), complaint);
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

        MatchTeam matchTeam = complaint.getMatchTeam();
        Match match = matchTeam.getMatch();
//        validateMatchAndTeams(matchTeam);

        complaint.setTitle(updateRequest.getTitle());
        complaint.setDescription(updateRequest.getDescription());
        complaint.setLastModifiedDate(new Date());

        complaint = complaintRepository.save(complaint);

        return mapToResponseDetails(matchTeam.getId(), complaint);
    }

//    private void validateMatchAndTeams(MatchTeam matchTeam) {
//        if (matchTeam == null) {
//            throw new ResourceNotFoundException("MatchTeam not found");
//        }
//        Match match = matchTeam.getMatch();
//        if (match == null) {
//            throw new ResourceNotFoundException("Match not found");
//        }
//        List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());
//        if (matchTeams.isEmpty()) {
//            throw new ResourceNotFoundException("No teams found for matchId: " + match.getId());
//        }
//
//        matchTeams.forEach(mt -> {
//            if (mt.getTeam() == null || mt.getAccount() == null) {
//                throw new ResourceNotFoundException("Invalid team or account for matchId: " + match.getId());
//            }
//        });
//    }
}


