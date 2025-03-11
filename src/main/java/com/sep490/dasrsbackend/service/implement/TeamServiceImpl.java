package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.entity.MatchTeam;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.TournamentRuleException;
import com.sep490.dasrsbackend.model.payload.response.MatchResponse;
import com.sep490.dasrsbackend.model.payload.response.TeamResponse;
import com.sep490.dasrsbackend.repository.AccountRepository;
import com.sep490.dasrsbackend.repository.MatchRepository;
import com.sep490.dasrsbackend.repository.MatchTeamRepository;
import com.sep490.dasrsbackend.repository.TeamRepository;
import com.sep490.dasrsbackend.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final ModelMapper modelMapper;
    private final MatchTeamRepository matchTeamRepository;
    private final AccountRepository accountRepository;
    private final MatchRepository matchRepository;

    @Override
    public List<MatchResponse> getMatches(Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Team not found, please contact administrator for more information"));

        List<MatchTeam> matchTeams = matchTeamRepository.findByTeamId(team.getId());
        List<Match> matches = new ArrayList<>();

        for (MatchTeam matchTeam : matchTeams) {
            matches.add(matchTeam.getMatch());
        }

        List<MatchResponse> matchesResponse = new ArrayList<>();
        for (Match match : matches) {
            MatchResponse matchResponse = modelMapper.map(match, MatchResponse.class);
            matchResponse.setTimeStart(DateUtil.formatTimestamp(match.getTimeStart()));
            matchResponse.setTimeEnd(DateUtil.formatTimestamp(match.getTimeEnd()));
            matchesResponse.add(matchResponse);
        }

        return matchesResponse;
    }

    @Override
    public void complainAboutMatch(Long teamId, String complaint) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        // Store the complaint in your database or handle as needed
        System.out.println("Complaint logged for team " + teamId + ": " + complaint);
    }

    @Override
    public void removeMember(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Optional<Account> member = team.getAccountList().stream()
                .filter(account -> account.getAccountId().equals(memberId))
                .findFirst();

        if (member.isEmpty()) {
            throw new IllegalArgumentException("Member not found in the team");
        }

        team.getAccountList().remove(member.get());
        member.get().setTeam(null); // Detach the member from the team
        teamRepository.save(team);
    }

//    @Override
//    public void selectMatchParticipants(Long teamId, List<Long> memberIds) {
//        Team team = teamRepository.findById(teamId)
//                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
//
//        List<Account> selectedMembers = team.getAccountList().stream()
//                .filter(account -> memberIds.contains(account.getAccountId()))
//                .collect(Collectors.toList());
//
//        if (selectedMembers.size() != memberIds.size()) {
//            throw new IllegalArgumentException("One or more members are not part of the team");
//        }
//
//        // Logic to associate selected members with the match
//        System.out.println("Selected participants for team " + teamId + ": " + memberIds);
//    }

    @Override
    public List<TeamResponse> getTeamMembers(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        return team.getAccountList().stream()
                .map(member -> new TeamResponse(member.getAccountId(), member.fullName()))
                .collect(Collectors.toList());
    }

    @Override
    public void transferLeadership(Long teamId, Long newLeaderId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Optional<Account> newLeader = team.getAccountList().stream()
                .filter(account -> account.getAccountId().equals(newLeaderId))
                .findFirst();

        if (newLeader.isEmpty()) {
            throw new IllegalArgumentException("New leader is not part of the team");
        }

        team.getAccountList().forEach(account -> account.setLeader(false)); // Reset leadership
        newLeader.get().setLeader(true); // Set new leader
        teamRepository.save(team);
    }

    @Override
    public void assignMemberToMatch(Long teamId, Long matchId, UUID assigner, UUID assignee) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Team not found, please contact administrator for more information"));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Match not found, please contact administrator for more information"));

        MatchTeam matchTeam = matchTeamRepository.findByTeamIdAndMatchId(team.getId(), match.getId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Match not found, please contact administrator for more information"));

        Account leader = accountRepository.findById(assigner)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Account not found, please contact administrator for more information"));

        Account member = accountRepository.findById(assignee)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Account not found, please contact administrator for more information"));

        if (!leader.isLeader()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assigner is not a leader");
        }

        if (leader.getTeam().getId() != member.getTeam().getId()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assignee is not in the same team");
        }

        // Đảm bảo tất cả thành viên đều tham gia trận đấu
        List<Account> availableMembers = accountRepository.findByTeamId(member.getTeam().getId());
        List<Account> existedMembers = new ArrayList<>();

        List<MatchTeam> matchTeams = matchTeamRepository.findByTeamId(team.getId());

        for (MatchTeam eachMatchTeam : matchTeams) {
            existedMembers.add(eachMatchTeam.getAccount());
        }

        for (Account existed : existedMembers) {
            availableMembers.remove(existed);
        }

        String message = availableMembers.stream()
                .map(Account::fullName)
                .collect(Collectors.joining(", "));

        Map<String, String> response = Map.of("available_members", message);

        if (!availableMembers.contains(member)) {
            throw new TournamentRuleException(HttpStatus.BAD_REQUEST,
                    "Assign fails. The number of times each member participates in the competition must be the same",
                    response);
        }
        matchTeam.setAccount(member);
        matchTeamRepository.save(matchTeam);
    }
}
