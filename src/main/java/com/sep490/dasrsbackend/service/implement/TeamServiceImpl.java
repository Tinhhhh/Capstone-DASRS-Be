package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.entity.MatchTeam;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.TournamentRuleException;
import com.sep490.dasrsbackend.model.payload.response.MatchResponse;
import com.sep490.dasrsbackend.model.payload.response.TeamMemberResponse;
import com.sep490.dasrsbackend.repository.AccountRepository;
import com.sep490.dasrsbackend.repository.MatchRepository;
import com.sep490.dasrsbackend.repository.MatchTeamRepository;
import com.sep490.dasrsbackend.repository.TeamRepository;
import com.sep490.dasrsbackend.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
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

        Account member = team.getAccountList().stream()
                .filter(account -> account.getAccountId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Member not found in the team"));

        if (member.isLocked()) {
            throw new IllegalArgumentException("Member is already locked");
        }

        member.setLocked(true); // Lock the account
        teamRepository.save(team);
    }

    @Override
    public void unlockMember(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Account member = team.getAccountList().stream()
                .filter(account -> account.getAccountId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Member not found in the team"));

        if (!member.isLocked()) {
            throw new IllegalArgumentException("Member is already unlocked");
        }

        member.setLocked(false); // Unlock the account
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
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        return team.getAccountList().stream()
                .map(member -> TeamMemberResponse.builder()
                        .id(member.getAccountId()) // UUID field
                        .fullName(member.fullName()) // Combines firstName + lastName
                        .build())
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

//    @Override
//    public void assignMemberToMatch(Long MatchTeamId, UUID assigner, UUID assignee) {
//
//        MatchTeam matchTeam = matchTeamRepository.findById(MatchTeamId)
//                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Match not found, please contact administrator for more information"));
//
//        Match match = matchTeam.getMatch();
//        Team team = matchTeam.getTeam();
//
//        Account leader = accountRepository.findById(assigner)
//                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Account not found, please contact administrator for more information"));
//
//        Account member = accountRepository.findById(assignee)
//                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Account not found, please contact administrator for more information"));
//
//        if (match.getStatus() == MatchStatus.UNASSIGNED) {
//            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Match has already started");
//        }
//
////        if (match.getTimeStart().before(DateUtil.getCurrentTimestamp())) {
////            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Match has already started");
////        }
//
//        if (!leader.isLeader()) {
//            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assigner is not a leader");
//        }
//
//        if (leader.getTeam().getId() != member.getTeam().getId()) {
//            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assignee is not in the same team");
//        }
//
//        // Đảm bảo tất cả thành viên đều tham gia trận đấu
//        List<Account> participatedMember = new ArrayList<>();
//
//        List<MatchTeam> matchTeams = matchTeamRepository.findByTeamId(team.getId());
//
//        for (MatchTeam eachMatchTeam : matchTeams) {
//            if (eachMatchTeam.getAccount() != null) {
//                participatedMember.add(eachMatchTeam.getAccount());
//            }
//        }
//
//        if (!participatedMember.isEmpty()) {
//            validateMemberParticipation(member, participatedMember);
//        }
//
//        matchTeam.setAccount(member);
//        matchTeamRepository.save(matchTeam);
//    }

    public void validateMemberParticipation(Account assignee, List<Account> teamMatches) {

        List<Account> members = accountRepository.findByTeamId(assignee.getTeam().getId());

        if (members.isEmpty()) {
            throw new TournamentRuleException(HttpStatus.BAD_REQUEST,
                    "Assign fails. No member found in the team");
        }

        Map<Account, Integer> participationCount = new HashMap<>();

        members.forEach(member -> participationCount.put(member, 0));
        // Đếm số lần tham gia của từng member
        for (Account acc : teamMatches) {
            participationCount.computeIfPresent(acc, (key, value) -> value + 1);
        }

        // Tìm min & max số lần tham gia
        int min = Collections.min(participationCount.values());
        int max = Collections.max(participationCount.values());

        Map<String, String> response = new HashMap<>();
        if (max - min <= 1) {
            List<String> availableMembers = participationCount.entrySet().stream()
                    .filter(entry -> entry.getValue() == min)  // Những người không bị dư số trận
                    .map(entry -> entry.getKey().fullName())
                    .collect(Collectors.toList());

            response = Map.of(
                    "available_members", String.join(", ", availableMembers)
            );
        } else {
            if (max - min > 1) {
                // Nếu max - min > 1 => lỗi
                throw new TournamentRuleException(HttpStatus.BAD_REQUEST,
                        "Assign fails. Please contact administrator for more information");
            }
        }

        // Nếu memberId không thuộc danh sách hợp lệ => lỗi
        if (participationCount.get(assignee) != min) {
            throw new TournamentRuleException(HttpStatus.BAD_REQUEST,
                    "Assign fails. The number of times each member participates in the competition must be the same" +
                            "User: " + assignee.fullName() + " is not eligible",
                    response);
        }
    }
}
