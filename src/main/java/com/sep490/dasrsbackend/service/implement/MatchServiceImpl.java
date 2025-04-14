package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.MatchSpecification;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.*;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.TournamentRuleException;
import com.sep490.dasrsbackend.model.payload.request.ChangeMatchSlot;
import com.sep490.dasrsbackend.model.payload.request.MatchCarData;
import com.sep490.dasrsbackend.model.payload.request.MatchScoreData;
import com.sep490.dasrsbackend.model.payload.request.UnityRoomRequest;
import com.sep490.dasrsbackend.model.payload.response.*;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.LeaderboardService;
import com.sep490.dasrsbackend.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final TournamentRepository tournamentRepository;
    private final LeaderboardRepository leaderboardRepository;

    private final LeaderboardService leaderboardService;

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(MatchServiceImpl.class);

    @Override
    public List<MatchResponseForTeam> getMatches(Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Team not found, please contact administrator for more information"));

        List<MatchTeam> matchTeams = matchTeamRepository.findByTeamId(team.getId()).stream()
                .filter(matchTeam -> matchTeam.getMatch().getStatus() != MatchStatus.TERMINATED).toList();

        List<Match> matches = matchTeams.stream().map(MatchTeam::getMatch).distinct().toList();
        List<MatchResponseForTeam> matchesResponse = new ArrayList<>();

        modelMapper.getConfiguration().setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setAmbiguityIgnored(true)
                .setSkipNullEnabled(false)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        for (Match match : matches) {
            MatchResponseForTeam matchResponse = modelMapper.map(match, MatchResponseForTeam.class);
            matchResponse.setMatchId(match.getId());
            matchResponse.setTeamId(teamId);
            matchResponse.setTeamName(team.getTeamName());
            matchResponse.setTeamTag(team.getTeamTag());
            matchResponse.setTimeStart(DateUtil.formatTimestamp(match.getTimeStart(), DateUtil.DATE_TIME_FORMAT));
            matchResponse.setTimeEnd(DateUtil.formatTimestamp(match.getTimeEnd(), DateUtil.DATE_TIME_FORMAT));

            List<MatchTeam> mt = matchTeamRepository.findByTeamIdAndMatchId(team.getId(), match.getId());
            List<MatchTeamResponse> matchTeamResponses = new ArrayList<>();
            for (MatchTeam matchTeam : mt) {
                MatchTeamResponse matchTeamResponse = new MatchTeamResponse();
                if (matchTeam.getAccount() == null) {
                    matchTeamResponse.setPlayerId(null);
                } else {
                    matchTeamResponse.setPlayerId(matchTeam.getAccount().getAccountId());
                }
                matchTeamResponse.setMatchTeamId(matchTeam.getId());
                matchTeamResponses.add(matchTeamResponse);
            }
            matchResponse.setMatchTeam(matchTeamResponses);
            matchesResponse.add(matchResponse);
        }

        return matchesResponse;
    }

    @Override
    public void assignMemberToMatch(Long matchTeamId, UUID assigner, UUID assignee) {

        MatchTeam matchTeam = matchTeamRepository.findById(matchTeamId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match not found, please contact administrator for more information"));

        Account leader = accountRepository.findById(assigner)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Account not found, please contact administrator for more information"));

        Account member = accountRepository.findById(assignee)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Account not found, please contact administrator for more information"));

        Team team = matchTeam.getTeam();
        Match match = matchTeam.getMatch();

        if (!leader.isLeader()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assigner is not a leader");
        }

        if (leader.getTeam().getId() != member.getTeam().getId()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assignee is not in the same team");
        }

        if (leader.getTeam().getId() != team.getId()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assigner is not in the leader of the current team");
        }

        if (match.getTimeStart().before(DateUtil.convertToDate(LocalDateTime.now()))) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Match has already started");
        }

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Match is not in pending status");
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
        matchTeam.setStatus(MatchTeamStatus.ASSIGNED);
        matchTeamRepository.save(matchTeam);
    }

    @Override
    public ListMatchResponse getMatchByRoundId(int pageNo, int pageSize, MatchSort sortBy, Long roundId, String keyword) {

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found, please contact administrator for more information"));

        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Specification<Match> matchSpecification = Specification.where(
                MatchSpecification.hasRoundId(round.getId()).and(MatchSpecification.hasMatchName(keyword))
        );

        Page<Match> matchPage = matchRepository.findAll(matchSpecification, pageable);
        List<Match> matches = matchPage.getContent();
        List<MatchResponse> matchResponses = new ArrayList<>();

        matches.forEach(match -> {
            MatchResponse matchResponse = getMatchResponse(match);
            matchResponses.add(matchResponse);
        });

        ListMatchResponse listMatchResponse = new ListMatchResponse();
        listMatchResponse.setContent(matchResponses);
        listMatchResponse.setPageNo(matchPage.getNumber());
        listMatchResponse.setPageSize(matchPage.getSize());
        listMatchResponse.setTotalElements(matchPage.getTotalElements());
        listMatchResponse.setTotalPages(matchPage.getTotalPages());
        listMatchResponse.setLast(matchPage.isLast());

        return listMatchResponse;
    }

    @Transactional
    @Override
    public void updateMatchTeamScore(MatchScoreData matchScoreData) {

        Match match = matchRepository.findByMatchCode(matchScoreData.getMatchCode())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match not found, please contact administrator for more information"));

        Account account = accountRepository.findById(matchScoreData.getPlayerId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Account not found, please contact administrator for more information"));

        Team team = account.getTeam();

        ScoreAttribute scoreAttribute = ScoreAttribute.builder()
                .lap(matchScoreData.getLap())
                .fastestLapTime(matchScoreData.getFastestLapTime())
                .collision(matchScoreData.getCollision())
                .totalRaceTime(matchScoreData.getTotalRaceTime())
                .offTrack(matchScoreData.getOffTrack())
                .assistUsageCount(matchScoreData.getAssistUsageCount())
                .topSpeed(matchScoreData.getTopSpeed())
                .averageSpeed(matchScoreData.getAverageSpeed())
                .totalDistance(matchScoreData.getTotalDistance())
                .build();

        scoreAttribute = scoreAttributeRepository.save(scoreAttribute);

        //Cập nhật vào match team => tạo instance, set score attr, set car config
        MatchTeam matchTeam = matchTeamRepository.findByTeamIdAndMatchIdAndAccountAccountId(team.getId(), match.getId(), account.getAccountId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "MatchTeam not found, please contact administrator for more information"));

        if (!matchTeam.getAccount().getAccountId().equals(matchScoreData.getPlayerId())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error, registered player with participated player not match, please contact administrator for more information");
        }

        //Cập nhật điểm matchTeam => matchTeam score, status
        Round round = roundRepository.findById(match.getRound().getId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, Round not found"));

        ScoredMethod sm = scoredMethodRepository.findById(round.getScoredMethod().getId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, Scored Method not found"));

        if (sm.getStatus() == ScoredMethodStatus.INACTIVE) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, Scored Method is inactive");
        }

        // Calculate score
        matchTeam.setScoreAttribute(scoreAttribute);
        double score = 0;

        ScoreAttribute sa = scoreAttributeRepository.findById(matchTeam.getScoreAttribute().getId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, Score Attribute not found"));

        score += calculateScore(sm, sa);
        matchTeam.setScore(score);
        matchTeamRepository.save(matchTeam);

        match.setStatus(MatchStatus.FINISHED);
        matchRepository.save(match);

        //cập nhật bảng xếp hạng leaderboard
        Optional<Leaderboard> lbOtp = leaderboardRepository.findByRoundIdAndTeamId(round.getId(), team.getId());
        Leaderboard lb = new Leaderboard();
        if (lbOtp.isPresent()) {
            lb = lbOtp.get();
            List<MatchTeam> matchTeams = matchTeamRepository.findByTeamIdAndMatchId(team.getId(), match.getId());
            double totalScore = 0;
            for (MatchTeam mt : matchTeams) {
                totalScore += mt.getScore();
            }
            lb.setTeamScore(totalScore);
        } else {
            lb.setTeam(team);
            lb.setRound(round);
            lb.setTeamScore(score);
        }

        leaderboardRepository.save(lb);
        leaderboardService.updateLeaderboard(round.getId());
    }

    @Override
    public void updateMatchTeamCar(MatchCarData matchCarData) {
        Match match = matchRepository.findByMatchCode(matchCarData.getMatchCode())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match not found, please contact administrator for more information"));

        Account account = accountRepository.findById(matchCarData.getPlayerId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Account not found, please contact administrator for more information"));

        Team team = account.getTeam();

        MatchTeam matchTeam = matchTeamRepository.findByTeamIdAndMatchIdAndAccountAccountId(team.getId(), match.getId(), account.getAccountId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "MatchTeam not found, please contact administrator for more information"));

        modelMapper.map(matchCarData, matchTeam);
        matchTeamRepository.save(matchTeam);
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
        isValidTimeRange(changeMatchSlot.getStartDate(), match);

        //Kiểm tra  xem có trùng với match nào không
        //Case 1: Ko Trùng thì đổi giờ của match hiện tại
        Match match2 = matchRepository.findByTimeStartAndStatus(changeMatchSlot.getStartDate(), MatchStatus.PENDING);

        if (match2 == null) {
            match.setTimeStart(changeMatchSlot.getStartDate());
            match.setTimeEnd(DateUtil.convertToDate(DateUtil.convertToLocalDateTime(changeMatchSlot.getStartDate()).plusMinutes(round.getRoundDuration())));
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

    @Override
    public List<MatchResponse> getMatchesByTournamentId(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Tournament not found"));

        List<Round> rounds = tournament.getRoundList();
        List<Match> matches = new ArrayList<>();

        for (Round round : rounds) {
            matches.addAll(round.getMatchList());
        }

        List<MatchResponse> matchResponses = new ArrayList<>();

        for (Match match : matches) {
            MatchResponse matchResponse = getMatchResponse(match);
            matchResponses.add(matchResponse);
        }

        return matchResponses;
    }

    private MatchResponse getMatchResponse(Match match) {
        MatchResponse matchResponse = modelMapper.map(match, MatchResponse.class);
        matchResponse.setTimeStart(DateUtil.formatTimestamp(match.getTimeStart(), DateUtil.DATE_TIME_FORMAT));
        matchResponse.setTimeEnd(DateUtil.formatTimestamp(match.getTimeEnd(), DateUtil.DATE_TIME_FORMAT));

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
            if (matchTeam.getTeam() != null) {
                Team team = matchTeam.getTeam();
                TeamTournamentResponse teamTournamentResponse = modelMapper.map(team, TeamTournamentResponse.class);

                if (matchTeam.getMatch().getId() == match.getId()) {
                    if (matchTeam.getAccount() != null) {
                        teamTournamentResponse.setAccountId(matchTeam.getAccount().getAccountId());
                    }
                }
                teams.add(teamTournamentResponse);
            }
        });

        if (!teams.isEmpty()) {
            matchResponse.setTeams(teams);
        }

        return matchResponse;
    }

    @Override
    public UnityMatchResponse getAvailableMatch(LocalDateTime date) {

        // Chuyển đổi LocalDateTime sang Date
        Date calendarDate = DateUtil.convertToDate(date);

        // Kiểm tra xem có tournament nào đang hoạt động không
        Tournament tournament = tournamentRepository.findByStatusAndStartDateBefore(TournamentStatus.ACTIVE, calendarDate)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "No match found due to no active tournament currently"));

        Round round = roundRepository.findByStatusAndStartDateBefore(RoundStatus.ACTIVE, calendarDate)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "No match found due to no active round currently"));

        String time = DateUtil.formatTimestamp(DateUtil.convertToDate(date), "yyyy-MM-dd HH:00:00");
        Optional<Match> matchOtp = matchRepository.findMatchByHour(time);

        if (matchOtp.isEmpty()) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "No match found in the current hour");
        }
        Match match = matchOtp.get();
        MatchResponse matchResponse = modelMapper.map(getMatchResponse(match), MatchResponse.class);
        UnityMatchResponse unityMatchResponse = modelMapper.map(matchResponse, UnityMatchResponse.class);
        unityMatchResponse.setMatchId(match.getId());
        unityMatchResponse.setResourceId(round.getResource().getId());
        unityMatchResponse.setResourceName(round.getResource().getResourceName());
        unityMatchResponse.setResourceType(round.getResource().getResourceType());
        unityMatchResponse.setEnvironmentId(round.getEnvironment().getId());
        unityMatchResponse.setEnvironmentName(round.getEnvironment().getName());

        return unityMatchResponse;
    }

    @Override
    public UnityRoomResponse isValidPlayerInMatch(UnityRoomRequest unityRoomRequest) {

        UnityRoomResponse unityRoomResponse = new UnityRoomResponse();
        unityRoomResponse.setSuccess(false);

        Optional<Match> matchOpt = matchRepository.findByMatchCode(unityRoomRequest.getMatchCode());
        if (matchOpt.isEmpty()) {
            unityRoomResponse.setMessage("Match not found");
            return unityRoomResponse;
        }

        Match match = matchOpt.get();

        if (match.getStatus() != MatchStatus.PENDING) {
            unityRoomResponse.setMessage("Match is not available");
            return unityRoomResponse;
        }

        List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());

        Account account = accountRepository.findById(unityRoomRequest.getAccountId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Account not found, please contact administrator for more information"));

        UUID accountId = unityRoomRequest.getAccountId();
        Long teamId = account.getTeam().getId();

        boolean isValidPlayer = false;

        for (MatchTeam matchTeam : matchTeams) {
            if (matchTeam.getTeam().getId().equals(teamId) && matchTeam.getAccount().getAccountId().equals(accountId)) {
                if (matchTeam.getAttempt() == 0) {
                    isValidPlayer = true;
                    break;
                } else {
                    unityRoomResponse.setMessage("You have already joined the match");
                    return unityRoomResponse;
                }
            }
        }

        if (!isValidPlayer) {
            unityRoomResponse.setMessage("You were not assigned to join the match");
            return unityRoomResponse;
        }

        if (match.getTimeStart().after(unityRoomRequest.getJoinTime())) {
            unityRoomResponse.setMessage("Match has not started yet");
            return unityRoomResponse;
        }

        if (match.getTimeEnd().before(unityRoomRequest.getJoinTime())) {
            unityRoomResponse.setMessage("Match has ended");
            return unityRoomResponse;
        }

        unityRoomResponse.setSuccess(true);
        unityRoomResponse.setMessage("Valid player");
        return unityRoomResponse;
    }

    private double calculateScore(ScoredMethod sm, ScoreAttribute sa) {

        double score = 0;
        score += sm.getLap() * sa.getLap();
        score += sm.getCollision() * sa.getCollision();
        score += sm.getOffTrack() * sa.getOffTrack();
        score += sm.getAssistUsageCount() * sa.getAssistUsageCount();
        score += sm.getAverageSpeed() * sa.getAverageSpeed();
        score += sm.getTotalDistance() * sa.getTotalDistance();
        score += sm.getTotalRaceTime() * sa.getTotalRaceTime();

        return score <= 0 ? 0 : score;
    }

    @Scheduled(cron = "1 0 * * * ?")
    public void detectUnassignedMatch() {
        logger.info("Detecting unassigned match task running at {}", LocalDateTime.now());
        logger.info("is working hours ?");

        LocalDateTime now = LocalDateTime.now();

        // Khoảng thời gian diễn ra trận đấu
        LocalTime morningStart = LocalTime.of(8, 00);  // 8:00 sáng
        LocalTime morningEnd = LocalTime.of(12, 10);   // 12:10 sáng
        LocalTime afternoonStart = LocalTime.of(13, 00); // 13:00 chiều
        LocalTime afternoonEnd = LocalTime.of(17, 10);  // 17:10 chiều

        LocalTime currentHrs = now.toLocalTime();

        // Kiểm tra nếu thời gian hiện tại nằm trong khoảng thời gian diễn ra trận đấu
        if ((currentHrs.isAfter(morningStart) && currentHrs.isBefore(morningEnd)) ||
                (currentHrs.isAfter(afternoonStart) && currentHrs.isBefore(afternoonEnd))) {
            logger.info("Working hours");
            logger.info("Checking for upcoming matches...");

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 1);
            calendar.set(Calendar.MILLISECOND, 0);

            Date date = calendar.getTime();
            Optional<Tournament> t = tournamentRepository.findByStatusAndStartDateBefore(TournamentStatus.ACTIVE, date);

            if (t.isPresent()) {
                Tournament tournament = t.get();
                List<Round> rounds = roundRepository.findByTournamentIdAndStatus(tournament.getId(), RoundStatus.ACTIVE);
//            List<Round> rounds = roundRepository.findByTournamentId(tournament.getId());

                if (!rounds.isEmpty()) {
                    List<Match> matches = matchRepository.findByRoundId(rounds.get(0).getId());

                    for (Match match : matches) {
                        if (match.getTimeEnd().before(DateUtil.convertToDate(LocalDateTime.now()))) {

                            List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());
                            Round round = roundRepository.findById(match.getRound().getId())
                                    .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

                            MatchType matchType = round.getMatchType();
                            int playerNumber = matchType.getPlayerNumber();

                            //Map<Team, Số matchTeam = unassigned>, Nếu 1 team có số matchTeam unassigned == playerNumber
                            // thì team đó sẽ đc xem như là unassign, bỏ cuộc
                            Map<Long, Integer> teamCountMap = new HashMap<>();

                            for (MatchTeam matchTeam : matchTeams) {
                                if (!teamCountMap.containsKey(matchTeam.getTeam().getId())) {
                                    teamCountMap.put(matchTeam.getTeam().getId(), 0);
                                }
                            }

                            for (Map.Entry<Long, Integer> entry : teamCountMap.entrySet()) {
                                Long teamId = entry.getKey();
                                Integer count = entry.getValue();

                                //Kiểm tra xem 1 team có bao nhiêu matchTeam = unassigned
                                for (MatchTeam matchTeam : matchTeams) {
                                    if (matchTeam.getTeam().getId().equals(teamId)) {
                                        if (matchTeam.getStatus() == MatchTeamStatus.UNASSIGNED) {
                                            count++;
                                        }
                                    }
                                }

                                teamCountMap.put(teamId, count);
                            }

                            for (Map.Entry<Long, Integer> entry : teamCountMap.entrySet()) {
                                Long teamId = entry.getKey();
                                Integer count = entry.getValue();

                                if (count == playerNumber) {
                                    //Team này đã bỏ cuộc
                                    match.setStatus(MatchStatus.FINISHED);
                                    matchRepository.save(match);
                                }
                            }

                        }
                    }
                } else {
                    logger.info("No active round found");
                }
            } else {
                logger.info("No active tournament found");
            }
        }
        logger.info("Detecting unassigned maps task finished at {}", LocalDateTime.now());
    }

    @Override
    public List<MatchResponse> getMatchByRoundIdAndPlayerId(Long roundId, UUID accountId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

        List<Match> matches = matchRepository.findMatchesByRoundIdAndAccountId(roundId, accountId);
        List<MatchResponse> matchResponses = new ArrayList<>();

        matches.forEach(match -> {
            MatchResponse matchResponse = getMatchResponse(match);
            matchResponses.add(matchResponse);
        });

        return matchResponses;
    }

    @Override
    public List<LeaderboardDetails> getMatchScoreDetails(Long matchId, Long teamId) {

        List<MatchTeam> matchTeams = matchTeamRepository.findByTeamIdAndMatchId(teamId, matchId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Team not found"));


        if (matchTeams.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "MatchTeam not found, please contact administrator for more information");
        }

        List<LeaderboardDetails> leaderboardDetails = new ArrayList<>();

        for (MatchTeam mt : matchTeams) {
            LeaderboardDetails details = new LeaderboardDetails();

            Optional<Leaderboard> lb = leaderboardRepository.findByRoundIdAndTeamId(match.getRound().getId(), team.getId());
            if (lb.isEmpty()) {
                if (match.getTimeEnd().after(DateUtil.convertToDate(LocalDateTime.now()))) {
                    throw new DasrsException(HttpStatus.BAD_REQUEST, "No data found !, Match not finished yet");
                } else {
                    throw new DasrsException(HttpStatus.BAD_REQUEST, "No data found !, Please wait for the leaderboard to be updated");
                }
            }

            Leaderboard leaderboard = lb.get();

            ScoreAttribute sa = mt.getScoreAttribute();

            Round round = roundRepository.findById(match.getRound().getId())
                    .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

            ScoredMethod sm = round.getScoredMethod();

            details.setPlayerId(mt.getAccount().getAccountId());
            details.setTeamId(mt.getTeam().getId());
            details.setPlayerName(mt.getAccount().fullName());
            details.setTeamName(mt.getTeam().getTeamName());
            details.setRanking(leaderboard.getRanking());
            details.setScore(mt.getScore());
            details.setLapScore(sa.getLap() * sm.getLap());
            details.setLap(sa.getLap());
            details.setFastestLapTime(sa.getFastestLapTime());
            details.setCollisionScore(sa.getCollision() * sm.getCollision());
            details.setCollision(sa.getCollision());
            details.setTotalRaceTimeScore(sa.getTotalRaceTime() * sm.getTotalRaceTime());
            details.setTotalRaceTime(sa.getTotalRaceTime());
            details.setOffTrackScore(sa.getOffTrack() * sm.getOffTrack());
            details.setOffTrack(sa.getOffTrack());
            details.setAssistUsageScore(sa.getAssistUsageCount() * sm.getAssistUsageCount());
            details.setAssistUsageCount(sa.getAssistUsageCount());
            details.setTopSpeed(sa.getTopSpeed());
            details.setAverageSpeedScore(sa.getAverageSpeed() * sm.getAverageSpeed());
            details.setAverageSpeed(sa.getAverageSpeed());
            details.setTotalDistanceScore(sa.getTotalDistance() * sm.getTotalDistance());
            details.setTotalDistance(sa.getTotalDistance());

            leaderboardDetails.add(details);

        }

        return leaderboardDetails;
    }

}
