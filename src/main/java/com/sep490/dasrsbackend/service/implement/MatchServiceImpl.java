package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import com.sep490.dasrsbackend.model.enums.RoleEnum;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.TournamentRuleException;
import com.sep490.dasrsbackend.model.payload.request.ChangeMatchSlot;
import com.sep490.dasrsbackend.model.payload.request.MatchDataRequest;
import com.sep490.dasrsbackend.model.payload.response.MatchResponse;
import com.sep490.dasrsbackend.model.payload.response.TeamTournamentResponse;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final TeamRepository teamRepository;
    private final ModelMapper modelMapper;
    private final MatchTeamRepository matchTeamRepository;
    private final MatchRepository matchRepository;
    private final AccountRepository accountRepository;
    private final RoundRepository roundRepository;
    private final ScoreAttributeRepository scoreAttributeRepository;
    private final ScoredMethodRepository scoredMethodRepository;
    private final MatchTypeRepository matchTypeRepository;

    @Override
    public List<MatchResponse> getMatches(Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Team not found, please contact administrator for more information"));

        List<MatchTeam> matchTeams = matchTeamRepository.findByTeamId(team.getId());
        List<Match> matches = new ArrayList<>();

        modelMapper.getConfiguration().setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setAmbiguityIgnored(true)
                .setSkipNullEnabled(false)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        for (MatchTeam matchTeam : matchTeams) {
            matches.add(matchTeam.getMatch());
        }

        List<MatchResponse> matchesResponse = new ArrayList<>();
        for (Match match : matches) {
            MatchResponse matchResponse = modelMapper.map(match, MatchResponse.class);
            matchResponse.setTimeStart(DateUtil.formatTimestamp(match.getTimeStart()));
            matchResponse.setTimeEnd(DateUtil.formatTimestamp(match.getTimeEnd()));
            List<TeamTournamentResponse> teams = new ArrayList<>();
            teams.add(modelMapper.map(team, TeamTournamentResponse.class));
            matchResponse.setTeams(teams);
            matchesResponse.add(matchResponse);
        }

        return matchesResponse;
    }


    @Override
    public void assignMemberToMatch(Long teamId, Long matchId, UUID assigner, UUID assignee) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Team not found, please contact administrator for more information"));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match not found, please contact administrator for more information"));

        MatchTeam matchTeam = matchTeamRepository.findByTeamIdAndMatchId(team.getId(), match.getId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match not found, please contact administrator for more information"));

        Account leader = accountRepository.findById(assigner)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Account not found, please contact administrator for more information"));

        Account member = accountRepository.findById(assignee)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Account not found, please contact administrator for more information"));

        if (!leader.isLeader()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assigner is not a leader");
        }

        if (leader.getTeam().getId() != member.getTeam().getId()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assignee is not in the same team");
        }

        if (leader.getTeam().getId() != team.getId()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assigner is not in the leader of the current team");
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

    @Override
    public List<MatchResponse> getMatchByRoundId(Long roundId) {

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found, please contact administrator for more information"));

        List<Match> matches = matchRepository.findByRoundId(round.getId());
        List<MatchResponse> matchResponses = new ArrayList<>();

        matches.forEach(match -> {
            MatchResponse matchResponse = modelMapper.map(match, MatchResponse.class);
            matchResponse.setTimeStart(DateUtil.formatTimestamp(match.getTimeStart()));
            matchResponse.setTimeEnd(DateUtil.formatTimestamp(match.getTimeEnd()));

            List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());

            if (matchTeams.isEmpty()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "MatchTeam not found, please contact administrator for more information");
            }

            modelMapper.getConfiguration().setFieldMatchingEnabled(true)
                    .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                    .setAmbiguityIgnored(true)
                    .setSkipNullEnabled(false)
                    .setMatchingStrategy(MatchingStrategies.STRICT);

            List<TeamTournamentResponse> teams = new ArrayList<>();

            matchTeams.forEach(matchTeam -> {
                Team team = matchTeam.getTeam();
                teams.add(modelMapper.map(team, TeamTournamentResponse.class));
            });

            matchResponse.setTeams(teams);
            matchResponses.add(matchResponse);
        });

        return matchResponses;
    }

    @Transactional
    @Override
    public void retrieveMatchData(MatchDataRequest matchDataRequest) {

        Match match = matchRepository.findById(matchDataRequest.getMatchId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match not found, please contact administrator for more information"));

        ScoreAttribute scoreAttribute = ScoreAttribute.builder()
                .lap(matchDataRequest.getLap())
                .fastestLapTime(matchDataRequest.getFastestLapTime())
                .collision(matchDataRequest.getCollision())
                .totalRaceTime(matchDataRequest.getTotalRaceTime())
                .offTrack(matchDataRequest.getOffTrack())
                .assistUsageCount(matchDataRequest.getAssistUsageCount())
                .topSpeed(matchDataRequest.getTopSpeed())
                .averageSpeed(matchDataRequest.getAverageSpeed())
                .totalDistance(matchDataRequest.getTotalDistance())
                .build();

        scoreAttributeRepository.save(scoreAttribute);

        //Cập nhật vào match team => tạo instance, set score attr, set car config
        MatchTeam matchTeam = matchTeamRepository.findByTeamIdAndMatchId(matchDataRequest.getTeamId(), matchDataRequest.getMatchId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "MatchTeam not found, please contact administrator for more information"));

        Round round = match.getRound();
        ScoredMethod scoredMethod = round.getScoredMethod();

        //Cập nhật điểm match => match score, status

        //Kiểm tra xem toàn bộ trận đấu đã kết thúc chưa => đổi status của round

        //cập nhật bảng xếp hạng leaderboard

        matchRepository.save(match);
    }

    @Override
    public void changeMatchSlot(Long matchId, ChangeMatchSlot changeMatchSlot) {

        Account account = accountRepository.findById(changeMatchSlot.getAccountId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Account not found, please contact administrator for more information"));

        if (!account.getRole().getRoleName().equals(RoleEnum.STAFF.name())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Change match slot fails. Only staff can change match slot");
        }

        //match cần đổi
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match not found, please contact administrator for more information"));

        Round round = match.getRound();

        MatchType matchType = matchTypeRepository.findById(round.getMatchType().getId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "MatchType not found, please contact administrator for more information"));

        isValidTimeRange(changeMatchSlot.getStartDate(), match);

        //Kiểm tra  xem có trùng với match nào không
        //Case 1: Ko Trùng thì đổi giờ của match hiện tại
        Match match2 = matchRepository.findByTimeStartAndStatus(changeMatchSlot.getStartDate(), MatchStatus.PENDING);

        if (match2 == null) {
            match.setTimeStart(changeMatchSlot.getStartDate());
            match.setTimeEnd(DateUtil.convertToDate(DateUtil.convertToLocalDateTime(changeMatchSlot.getStartDate()).plusMinutes((long) matchType.getMatchDuration())));
            matchRepository.save(match);
        }
//        Case 2: Trùng thì swap giờ với match đó
        if (match2 != null) {
            Date tempStart = match.getTimeStart();
            Date tempEnd = match.getTimeEnd();

            match.setTimeStart(match2.getTimeStart());
            match.setTimeEnd(match2.getTimeEnd());

            match2.setTimeStart(tempStart);
            match2.setTimeEnd(tempEnd);

            matchRepository.save(match);
            matchRepository.save(match2);
        }

    }

    public void isValidTimeRange(Date start, Match match) {

        LocalDateTime startDateTime = DateUtil.convertToLocalDateTime(start);

        int startHour = startDateTime.getHour();
        int startMinute = startDateTime.getMinute();

        // Kiểm tra cả hai phải là giờ chẵn và nằm trong khoảng hợp lệ
        boolean isValidStart = (startMinute == 0) && ((startHour >= 8 && startHour <= 11) || (startHour >= 13 && startHour <= 16));

        if (!isValidStart) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "Change match slot fails. The time slot start must be between 8:00 - 11:00 and 13:00 - 16:00 " +
                            "and the start time minute must be 0");
        }

        Round round = match.getRound();

        if (start.before(round.getStartDate())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "Change match slot fails. The start date must be after the round start date");
        }



    }






}
