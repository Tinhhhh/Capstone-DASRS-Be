package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.*;
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
import java.util.logging.Logger;
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
    private static final Logger logger = Logger.getLogger(TeamServiceImpl.class.getName());
    private final LeaderboardRepository leaderboardRepository;
    private final RoundRepository roundRepository;
    private final TournamentRepository tournamentRepository;

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
            throw new InvalidOperationException("Cannot join a team currently participating in a tournament");
        }

        if (player.getTeam() != null) {
            throw new InvalidOperationException("Player is already part of a team");
        }

        if (team.getAccountList().size() >= 5) {
            throw new InvalidOperationException("Team cannot have more than 5 members");
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
            throw new InvalidOperationException("Cannot join a team currently participating in a tournament");
        }

        if (newTeam.getAccountList().size() >= 5) {
            throw new InvalidOperationException("Team cannot have more than 5 members");
        }

        Team currentTeam = player.getTeam();
        if (currentTeam != null && !tournamentTeamRepository.findByTeamAndTournamentNotNull(currentTeam).isEmpty()) {
            throw new InvalidOperationException("Cannot leave a team currently participating in a tournament");
        }

        player.setTeam(newTeam);
        accountRepository.save(player);
    }


    @Override
    public void leaveTeam(Long teamId, UUID playerId) {
        Account player = accountRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        Team team = player.getTeam();

        if (team == null || !team.getId().equals(teamId)) {
            throw new InvalidOperationException("Player is not part of the specified team");
        }

        if (!tournamentTeamRepository.findByTeamAndTournamentNotNull(team).isEmpty()) {
            throw new InvalidOperationException("Cannot leave a team currently in a tournament");
        }

        if (player.isLeader() && team.getAccountList().size() > 1) {
            throw new InvalidOperationException("Leader cannot leave a team with more than one other member");
        }

        player.setTeam(null);
        accountRepository.save(player);
    }


    @Override
    public void deleteTeam(Long teamId, UUID leaderId) {
        try {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new IllegalArgumentException("Team not found"));

            Account leader = team.getAccountList().stream()
                    .filter(account -> account.getAccountId().equals(leaderId) && account.isLeader())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Leader not found for the specified team"));

            boolean isSoleLeader = team.getAccountList().size() == 1;

            boolean hasParticipated = tournamentTeamRepository.existsByTeam(team);

            if (isSoleLeader && !hasParticipated) {
                leaderboardRepository.deleteAllByTeam(team);
                matchTeamRepository.deleteAllByTeam(team);
                tournamentTeamRepository.deleteAllByTeam(team);

                team.getAccountList().forEach(account -> {
                    account.setTeam(null);
                    account.setLeader(false);
                });
                accountRepository.saveAll(team.getAccountList());

                teamRepository.delete(team);
                logger.info("Team with only one member successfully deleted.");
            } else if (isSoleLeader && hasParticipated) {
                leader.setTeam(null);
                leader.setLeader(false);
                accountRepository.save(leader);

                team.setStatus(TeamStatus.INACTIVE);
                teamRepository.save(team);
                logger.info("Team has been marked as INACTIVE, and the leader has been dissociated.");
            } else if (team.getAccountList().size() > 1) {
                throw new IllegalArgumentException("Cannot delete a team with more than one member.");
            } else {
                throw new IllegalArgumentException("Cannot delete a team that is currently participating in a tournament.");
            }
        } catch (IllegalArgumentException e) {
            logger.severe("Validation error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.severe("An unexpected error occurred while deleting the team: " + e.getMessage());
            throw new RuntimeException("Failed to delete the team due to an internal error.");
        }
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

    @Override
    public List<TeamResponse> getTeamsByRoundId(Long roundId) {

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Round not found"));

        List<Team> teams = leaderboardRepository.findByRoundId(roundId)
                .stream()
                .map(Leaderboard::getTeam)
                .toList();

        List<TeamResponse> teamResponses = new ArrayList<>();
        for (Team team : teams) {
            TeamResponse teamResponse = modelMapper.map(team, TeamResponse.class);
            teamResponse.setMemberCount(team.getAccountList() != null ? team.getAccountList().size() : 0);
            teamResponses.add(teamResponse);
        }

        return teamResponses;
    }

    @Override
    public List<TeamResponse> getTeamsByTournamentId(Long tournamentId) {

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Tournament not found"));

        List<Team> teams = tournamentTeamRepository.findByTournamentId(tournamentId)
                .stream()
                .map(TournamentTeam::getTeam)
                .distinct()
                .toList();

        List<TeamResponse> teamResponses = new ArrayList<>();
        for (Team team : teams){
            TeamResponse teamResponse = modelMapper.map(team, TeamResponse.class);
            teamResponse.setMemberCount(team.getAccountList() != null ? team.getAccountList().size() : 0);
            teamResponses.add(teamResponse);
        }

        return teamResponses;
    }

    @Override
    public List<TeamMemberResponse> getTeamMembersByTeamIdAndMatchId(Long teamId, Long matchId) {
        if (teamId == null || matchId == null) {
            throw new IllegalArgumentException("Team ID and Match ID cannot be null");
        }

        matchRepository.findById(matchId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match not found"));

        List<MatchTeam> matchTeams = matchTeamRepository.findByTeamIdAndMatchId(teamId, matchId);
        if (matchTeams.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "No members found for the specified team and match");
        }

        return matchTeams.stream()
                .map(matchTeam -> {
                    Account account = matchTeam.getAccount();
                    return new TeamMemberResponse(
                            account.getAccountId(),
                            account.fullName(),
                            account.isLeader()
                    );
                })
                .collect(Collectors.toList());
    }

}
