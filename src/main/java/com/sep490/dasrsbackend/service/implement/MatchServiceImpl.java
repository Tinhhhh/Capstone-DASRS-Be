package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.MatchSpecification;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.*;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.TournamentRuleException;
import com.sep490.dasrsbackend.model.payload.request.MatchCarData;
import com.sep490.dasrsbackend.model.payload.request.MatchScoreData;
import com.sep490.dasrsbackend.model.payload.request.UnityRoomRequest;
import com.sep490.dasrsbackend.model.payload.response.*;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.LeaderboardService;
import com.sep490.dasrsbackend.service.MatchService;
import com.sep490.dasrsbackend.service.RoundUtilityService;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(MatchServiceImpl.class);
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
    private final TournamentTeamRepository tournamentTeamRepository;
    private final RoundUtilityService roundUtilityService;
    private final LeaderboardServiceImpl leaderboardServiceImpl;

    @Override
    public List<MatchResponseForTeam> getMatches(Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Server internal error. Team not found, please contact administrator for more information"));

        List<MatchTeam> matchTeams = matchTeamRepository.findByTeamId(team.getId()).stream()
                .filter(matchTeam -> matchTeam.getMatch().getStatus() != MatchStatus.TERMINATED).toList();

        List<Match> matches = new ArrayList<>();
        if (!matchTeams.isEmpty()) {
            matches = matchTeams.stream().map(MatchTeam::getMatch).distinct().toList();
        }
        List<MatchResponseForTeam> matchesResponse = new ArrayList<>();

        if (!matches.isEmpty()) {
            matchesResponse = getMatchResponseForTeamList(matches, team);
        }

        return matchesResponse;
    }

    private List<MatchResponseForTeam> getMatchResponseForTeamList(List<Match> matches, Team team) {

        modelMapper.getConfiguration().setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setAmbiguityIgnored(true)
                .setSkipNullEnabled(false)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        List<MatchResponseForTeam> matchesResponse = new ArrayList<>();
        for (Match match : matches) {
            MatchResponseForTeam matchResponse = modelMapper.map(match, MatchResponseForTeam.class);
            matchResponse.setMatchId(match.getId());
            matchResponse.setTeamId(team.getId());
            matchResponse.setTeamName(team.getTeamName());
            matchResponse.setTeamTag(team.getTeamTag());
            matchResponse.setTimeStart(DateUtil.formatTimestamp(match.getTimeStart(), DateUtil.DATE_TIME_FORMAT));
            matchResponse.setTimeEnd(DateUtil.formatTimestamp(match.getTimeEnd(), DateUtil.DATE_TIME_FORMAT));
            matchResponse.setMatchForm(match.getMatchForm());

            List<MatchTeam> mt = matchTeamRepository.findByTeamIdAndMatchId(team.getId(), match.getId());
            List<MatchTeamResponse> matchTeamResponses = new ArrayList<>();
            for (MatchTeam matchTeam : mt) {
                MatchTeamResponse matchTeamResponse = new MatchTeamResponse();
                if (matchTeam.getAccount() == null) {
                    matchTeamResponse.setPlayerId(null);
                    matchTeamResponse.setPlayerName(null);
                } else {
                    matchTeamResponse.setPlayerId(matchTeam.getAccount().getAccountId());
                    matchTeamResponse.setPlayerName(matchTeam.getAccount().fullName());
                }
                matchTeamResponse.setMatchTeamId(matchTeam.getId());
                matchTeamResponse.setStatus(matchTeam.getStatus());
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
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assignee is not in the same team with assigner");
        }

        if (leader.getTeam().getId() != team.getId()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Assigner is neither leader nor member of the current match");
        }

        if (match.getTimeStart().before(DateUtil.convertToDate(LocalDateTime.now()))) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Match has already started");
        }

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Assign fails. Match is not in pending status");
        }


        // Đảm bảo tất cả thành viên đều tham gia trận đấu
        Tournament tournament = match.getRound().getTournament();

        List<Account> asssignedMember = new ArrayList<>();
        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournament.getId());
        for (Round round : rounds) {
            List<Match> matches = matchRepository.findByRoundId(round.getId()).stream()
                    .filter(match1 -> match1.getStatus() != MatchStatus.TERMINATED && match1.getMatchForm() == MatchForm.OFFICIAL).toList();
            for (Match m : matches) {
                List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(m.getId()).stream().filter(matchTeam1 -> matchTeam1.getStatus() == MatchTeamStatus.ASSIGNED).toList();
                for (MatchTeam mt : matchTeams) {
                    if (mt.getAccount() != null) {
                        asssignedMember.add(mt.getAccount());
                    }
                }
            }
        }

        if (!asssignedMember.isEmpty()) {
            validateMemberParticipation(member, asssignedMember);
        }
        matchTeam.setAccount(member);
        matchTeam.setStatus(MatchTeamStatus.ASSIGNED);
        matchTeamRepository.save(matchTeam);
    }

    public void validateMemberParticipation(Account assignee, List<Account> assignedMember) {

        List<Account> members = accountRepository.findByTeamId(assignee.getTeam().getId());

        if (members.isEmpty()) {
            throw new TournamentRuleException(HttpStatus.BAD_REQUEST,
                    "Assign fails. No member found in the team");
        }

        Map<Account, Integer> participationCount = new HashMap<>();

        members.forEach(member -> participationCount.put(member, 0));
        // Đếm số lần tham gia của từng member
        for (Account acc : assignedMember) {
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
        matchTeam.setStatus(MatchTeamStatus.COMPLETED);
        matchTeam.setScore(score);
        matchTeamRepository.save(matchTeam);

        boolean isMatchCompleted = matchTeamRepository.findByMatchId(match.getId()).stream()
                .filter(matchTeam1 -> matchTeam1.getStatus() != MatchTeamStatus.TERMINATED)
                .allMatch(matchTeam1 -> matchTeam1.getStatus() == MatchTeamStatus.COMPLETED);

        if (isMatchCompleted) {
            match.setStatus(MatchStatus.FINISHED);
            matchRepository.save(match);
        }


        //cập nhật bảng xếp hạng leaderboard
        Optional<Leaderboard> lbOtp = leaderboardRepository.findByRoundIdAndTeamId(round.getId(), team.getId());
        Leaderboard lb = new Leaderboard();
        if (lbOtp.isPresent()) {
            lb = lbOtp.get();

            List<Match> matches = matchRepository.findByRoundId(round.getId()).stream()
                    .filter(match1 -> match1.getStatus() != MatchStatus.TERMINATED).toList();

            List<MatchTeam> matchTeams = matches.stream()
                    .flatMap(match1 -> matchTeamRepository.findByMatchId(match1.getId()).stream())
                    .filter(matchTeam1 -> matchTeam1.getStatus() != MatchTeamStatus.TERMINATED)
                    .filter(matchTeam1 -> matchTeam1.getTeam().getId() == team.getId())
                    .toList();

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
    public void changeMatchSlot(Long matchId, LocalDateTime newDate) {

        //match cần đổi
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match not found, please contact administrator for more information"));

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Change match slot fails. Match is completed or terminated");
        }


        Date newStartDate = DateUtil.convertToDate(newDate);

        Round round = match.getRound();
        isValidTimeRange(newStartDate, match);

        //Kiểm tra  xem có trùng với match nào không
        //Case 1: Ko Trùng thì đổi giờ của match hiện tại
        Match match2 = matchRepository.findByTimeStartAndStatus(newStartDate, MatchStatus.PENDING);

        if (match2 == null) {
            match.setTimeStart(newStartDate);
            match.setTimeEnd(DateUtil.convertToDate(DateUtil.convertToLocalDateTime(newStartDate).plusMinutes(round.getRoundDuration())));
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
        //Check if round is start
        Calendar calendar = Calendar.getInstance();
        if (match.getTimeStart().after(calendar.getTime()) && match.getTimeEnd().before(calendar.getTime())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "Change match slot fails. The match is already started");
        }

        if (match.getTimeEnd().before(calendar.getTime())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "Change match slot fails. The match is already finished");
        }

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

        if (start.before(round.getStartDate()) && start.after(round.getEndDate())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "Change match slot fails. The start date must be between the round start date and end date");
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

    public MatchResponse getMatchResponse(Match match) {
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
    public UnityMatchResponse getAvailableMatch(Long tournamentId, LocalDateTime date) {

        // Chuyển đổi LocalDateTime sang Date
        Date time = DateUtil.convertToDate(date);

        List<Match> matchOtp = matchRepository.findByMatchByTime(time).stream()
                .filter(match -> match.getStatus() == MatchStatus.PENDING && match.getRound().getTournament().getId() == tournamentId)
                .toList();

        if (matchOtp.isEmpty()) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "No match found in the current time");
        }

        Match match = matchOtp.get(0);
        Round round = match.getRound();
        if (round.getStatus() != RoundStatus.ACTIVE) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "match is not available due to round status");
        }

        if (match.getRound().getTournament().getStatus() != TournamentStatus.ACTIVE) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "match is not available due to tournament status");
        }

        if (round.getTournament().getId() != tournamentId) {
            throw new DasrsException(HttpStatus.NOT_FOUND, "match is not available due to");
        }

        MatchResponse matchResponse = modelMapper.map(getMatchResponse(match), MatchResponse.class);
        UnityMatchResponse unityMatchResponse = modelMapper.map(matchResponse, UnityMatchResponse.class);
        unityMatchResponse.setMatchId(match.getId());
        unityMatchResponse.setScoredMethodId(round.getScoredMethod().getId());
        unityMatchResponse.setRoundDuration(round.getRoundDuration());
        unityMatchResponse.setLapNumber(round.getLapNumber());
        unityMatchResponse.setFinishType(round.getFinishType());
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

        List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId()).stream()
                .filter(matchTeam -> matchTeam.getStatus() == MatchTeamStatus.ASSIGNED).toList();

        Account account = accountRepository.findById(unityRoomRequest.getAccountId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Account not found, please contact administrator for more information"));

        UUID accountId = unityRoomRequest.getAccountId();
        Long teamId = account.getTeam().getId();

        boolean isValidPlayer = false;

        for (MatchTeam matchTeam : matchTeams) {
            if (matchTeam.getTeam().getId().equals(teamId) && matchTeam.getAccount().getAccountId().equals(accountId)) {
                isValidPlayer = true;
                break;
            }
        }

        if (!isValidPlayer) {
            unityRoomResponse.setMessage("Player were not assigned to join this match");
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

        Round round = match.getRound();

        unityRoomResponse.setSuccess(true);
        unityRoomResponse.setMatchCode(match.getMatchCode());
        unityRoomResponse.setScoredMethodId(round.getScoredMethod().getId());
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
    //Background service này dùng để đánh status các trận đấu đã kết thúc nhưng ko đc update kết quả trận đấu.

    @Transactional
    @Scheduled(cron = "1 0 * * * ?")
    public void detectNotFinishMatch() {
        logger.info("Detecting not finished match task running at {}", LocalDateTime.now());
        logger.info("Checking for upcoming matches...");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();
        List<Tournament> tournaments = tournamentRepository.findByStatusAndStartDateBefore(TournamentStatus.ACTIVE, date);

        if (!tournaments.isEmpty()) {
            for (Tournament t : tournaments) {

                List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(t.getId());
                if (!rounds.isEmpty()) {
                    for (Round r : rounds) {
                        //Tìm xem match nào đã kết thúc
                        List<Match> matches = matchRepository.findByRoundId(r.getId());
                        for (Match match : matches) {
                            if (match.getTimeEnd().before(DateUtil.convertToDate(LocalDateTime.now())) && match.getStatus() == MatchStatus.PENDING) {
//                            if (match.getStatus() == MatchStatus.PENDING) {

                                List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId()).stream()
                                        .filter(matchTeam -> matchTeam.getStatus() != MatchTeamStatus.TERMINATED).toList();

                                for (MatchTeam matchTeam : matchTeams) {
                                    matchTeam.setStatus(MatchTeamStatus.COMPLETED);
                                    matchTeamRepository.save(matchTeam);
                                }

                                match.setStatus(MatchStatus.FINISHED);
                                matchRepository.save(match);
                                logger.info("Match {} has been set to finished", match.getId());
                            } else {
                                logger.info("Match {} is still in progress", match.getId());
                            }


                            List<Team> teams = matchTeamRepository.findByMatchId(match.getId()).stream()
                                    .map(MatchTeam::getTeam)
                                    .filter(Objects::nonNull)
                                    .distinct()
                                    .toList();

                            for (Team team : teams) {
                                //Kiểm tra xem team này đã có leaderboard chưa
                                Optional<Leaderboard> lbOtp = leaderboardRepository.findByRoundIdAndTeamId(r.getId(), team.getId());
                                if (lbOtp.isEmpty()) {
                                    Leaderboard lb = new Leaderboard();
                                    lb.setTeam(team);
                                    lb.setRound(r);
                                    lb.setTeamScore(0);
                                    lb.setRanking(0);
                                    leaderboardRepository.save(lb);
                                }
                            }

                            //Cập nhật lại leaderboard
                            leaderboardServiceImpl.updateLeaderboard(r.getId());
                        }
                    }
                } else {
                    logger.info("No active round found");
                }
            }
        } else {
            logger.info("No active tournament found");
        }
        logger.info("Detecting not finished match task finished at {}", LocalDateTime.now());
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

            if (mt.getScoreAttribute() == null) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Match is not finish or score is not updated");
            }
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

    @Transactional
    @Override
    public void createMatch(List<Long> matchTeamIds) {

        List<MatchTeam> matchTeams = matchTeamRepository.findAllById(matchTeamIds);
        boolean sameRound = matchTeams.stream().map(m -> m.getMatch().getRound())
                .distinct()
                .count() == 1;

        if (!sameRound) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "All the teams are not in the same round");
        }

        List<String> attemptedTeam = new ArrayList<>();
        for (MatchTeam matchTeam : matchTeams) {
            if (matchTeam.getAttempt() != 0) {
                attemptedTeam.add(matchTeam.getId().toString());
            }

            Match match = matchTeam.getMatch();

            if (match.getMatchForm() == MatchForm.REMATCH){
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, match with id " + match.getId() + " is alr rematch, cannot rematch again");
            }

        }

        if (!attemptedTeam.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "These matchTeamId had already been rematch once: " + String.join(", ", attemptedTeam));
        }

        roundUtilityService.generateRematch(matchTeams);
        for (MatchTeam matchTeam : matchTeams) {
            matchTeam.setAttempt(1);
            matchTeamRepository.save(matchTeam);
        }

    }

    @Override
    public List<MatchResponseForTeam> getMatchByTeamIdAndRoundId(Long teamId, Long roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Team not found"));

        List<MatchTeam> matchTeams = matchTeamRepository.findByTeamId(teamId).stream()
                .filter(matchTeam -> matchTeam.getMatch().getRound().getId() == roundId).toList();
        List<Match> matches = new ArrayList<>();
        if (!matchTeams.isEmpty()) {
            matches = matchTeams.stream()
                    .map(MatchTeam::getMatch)
                    .filter(Match -> Match.getStatus() != MatchStatus.TERMINATED)
                    .distinct().toList();
        }

        List<MatchResponseForTeam> matchResponses = new ArrayList<>();

        if (!matches.isEmpty()) {
            matchResponses = getMatchResponseForTeamList(matches, team);
        }

        return matchResponses;
    }

}
