package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.entity.MatchTeam;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import com.sep490.dasrsbackend.model.enums.TeamStatus;
import com.sep490.dasrsbackend.model.exception.*;
import com.sep490.dasrsbackend.model.payload.response.MatchResponse;
import com.sep490.dasrsbackend.model.payload.response.TeamMemberResponse;
import com.sep490.dasrsbackend.model.payload.response.TeamResponse;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
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
    private final TournamentTeamRepository tournamentTeamRepository;

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
    public void removeMember(Long teamId, UUID memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Account member = team.getAccountList().stream()
                .filter(account -> account.getAccountId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Member not found in the team"));

        if (!tournamentTeamRepository.findByTeamAndTournamentNotNull(team).isEmpty()) {
            throw new IllegalArgumentException("Cannot remove member from a team currently in a tournament");
        }
        member.setTeam(null);
        accountRepository.save(member);
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
                        .id(member.getAccountId())
                        .fullName(member.fullName())
                        .isLeader(member.isLeader())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void transferLeadership(Long teamId, UUID newLeaderId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Account currentLeader = team.getAccountList().stream()
                .filter(Account::isLeader)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No current leader found in the team"));

        Account newLeader = team.getAccountList().stream()
                .filter(account -> account.getAccountId().equals(newLeaderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("New leader is not part of the team"));

        if (!currentLeader.equals(newLeader)) {
            currentLeader.setLeader(false);
            newLeader.setLeader(true);
            accountRepository.save(currentLeader);
            accountRepository.save(newLeader);
        }
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

    @Override
    public TeamResponse getTeamDetails(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Team not found"));

        Optional<Account> leader = team.getAccountList().stream()
                .filter(Account::isLeader)
                .findFirst();

        if (leader.isEmpty()) {
            throw new DasrsException(HttpStatus.CONFLICT, "Leader not assigned for this team");
        }

        List<TeamMemberResponse> members = team.getAccountList().stream()
                .map(account -> TeamMemberResponse.builder()
                        .id(account.getAccountId())
                        .fullName(account.fullName())
                        .build())
                .collect(Collectors.toList());

        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getTeamName())
                .tag(team.getTeamTag())
                .disqualified(team.isDisqualified())
                .status(team.getStatus())
                .memberCount(team.getAccountList() != null ? team.getAccountList().size() : 0)
                .build();
    }
    @Override
    public List<TeamResponse> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        if (teams.isEmpty()) {
            throw new TeamNotFoundException("No teams found!");
        }

        return teams.stream()
                .map(team -> {
                    if (team.getTeamName() == null || team.getTeamName().isBlank()) {
                        throw new InvalidTeamDataException(
                                String.format("Team with ID %d has an invalid name", team.getId()));
                    }

                    if (team.getStatus() == null) {
                        throw new InvalidTeamDataException(
                                String.format("Team with ID %d has an invalid status", team.getId()));
                    }

                    return TeamResponse.builder()
                            .id(team.getId())
                            .name(team.getTeamName())
                            .tag(team.getTeamTag() != null ? team.getTeamTag() : "N/A")
                            .disqualified(team.isDisqualified())
                            .status(team.getStatus())
                            .memberCount(team.getAccountList() != null ? team.getAccountList().size() : 0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void joinTeam(Long teamId, UUID playerId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));
        Account player = accountRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        if (!tournamentTeamRepository.findByTeamAndTournamentNotNull(team).isEmpty()) {
            throw new InvalidOperationException("Cannot join a team currently in a tournament");
        }

        if (player.getTeam() != null) {
            throw new InvalidOperationException("Player is already part of a team");
        }

        player.setTeam(team);
        accountRepository.save(player);
    }

    @Override
    public void changeTeam(Long teamId, UUID playerId) {
        Team newTeam = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));
        Account player = accountRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        if (player.isLeader()) {
            throw new InvalidOperationException("Team leader cannot change teams");
        }

        if (!tournamentTeamRepository.findByTeamAndTournamentNotNull(newTeam).isEmpty()) {
            throw new InvalidOperationException("Cannot join a team currently in a tournament");
        }

        player.setTeam(newTeam);
        accountRepository.save(player);
    }

    @Override
    public void leaveTeam(Long teamId, UUID playerId) {
        Account player = accountRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        if (player.getTeam() == null || !player.getTeam().getId().equals(teamId)) {
            throw new InvalidOperationException("Player is not part of the specified team");
        }

        if (!tournamentTeamRepository.findByTeamAndTournamentNotNull(player.getTeam()).isEmpty()) {
            throw new InvalidOperationException("Cannot leave a team currently in a tournament");
        }

        player.setTeam(null);
        accountRepository.save(player);
    }

    @Override
    public void deleteTeam(Long teamId, UUID leaderId) {
//        Team team = teamRepository.findById(teamId)
//                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
//
//        Account leader = team.getAccountList().stream()
//                .filter(account -> account.getAccountId().equals(leaderId) && account.isLeader())
//                .findFirst()
//                .orElseThrow(() -> new IllegalArgumentException("Leader not found"));
//
//        if (!tournamentTeamRepository.findByTeamAndTournamentNotNull(team).isEmpty()) {
//            throw new IllegalArgumentException("Cannot delete a team currently involved in a tournament");
//        }
//        if (team.getAccountList().size() > 1) {
//            throw new IllegalArgumentException("Team has more than one member. Cannot delete a team with more than just the leader.");
//        }
//
//        team.setStatus(TeamStatus.TERMINATED);
//
//        teamRepository.save(team);
    }

    @Override
    public void createTeam(UUID playerId, String teamName, String teamTag) {
        Account player = accountRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        if (player.getTeam() != null) {
            throw new IllegalArgumentException("Player is already part of a team");
        }

        boolean isTagTaken = teamRepository.existsByTeamTag(teamTag);
        if (isTagTaken) {
            throw new IllegalArgumentException("Team tag is already in use");
        }

        Team team = new Team();
        team.setTeamName(teamName);
        team.setTeamTag(teamTag);
        team.setStatus(TeamStatus.ACTIVE);
        team.setDisqualified(false);
        player.setLeader(true);
        player.setTeam(team);

        teamRepository.save(team);
        accountRepository.save(player);
    }
}
