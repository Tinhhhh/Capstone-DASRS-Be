package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.GenerateCode;
import com.sep490.dasrsbackend.Util.RoundSpecification;
import com.sep490.dasrsbackend.Util.Schedule;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.*;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.EditRound;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.model.payload.response.GetPlayerRoundResponse;
import com.sep490.dasrsbackend.model.payload.response.GetRoundsByAccountResponse;
import com.sep490.dasrsbackend.model.payload.response.ListRoundResponse;
import com.sep490.dasrsbackend.model.payload.response.RoundResponse;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.RoundService;
import lombok.AllArgsConstructor;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class RoundServiceImpl implements RoundService {

    private final TournamentRepository tournamentRepository;
    private final RoundRepository roundRepository;
    private final MatchTypeRepository matchTypeRepository;
    private final EnvironmentRepository environmentRepository;
    private final ScoredMethodRepository scoredMethodRepository;
    private final ModelMapper modelMapper;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final ScoreAttributeRepository scoreAttributeRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final ResourceRepository resourceRepository;
    private final AccountRepository accountRepository;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RoundServiceImpl.class);
    private final TournamentServiceImpl tournamentServiceImpl;
    private final TournamentTeamRepository tournamentTeamRepository;

    @Transactional
    @Override
    public void newRound(NewRound newRound) {

        newRound.setStartDate(DateUtil.convertToStartOfTheDay(newRound.getStartDate()));
        newRound.setEndDate(DateUtil.convertToEndOfTheDay(newRound.getEndDate()));

        Tournament tournament = tournamentRepository.findById(newRound.getTournamentId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Tournament not found"));

        MatchType matchType = matchTypeRepository.findByIdAndStatus(newRound.getMatchTypeId(), MatchTypeStatus.ACTIVE)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match Type not found"));

        ScoredMethod scoredMethod = scoredMethodRepository.findByIdAndStatus(newRound.getScoredMethodId(), ScoredMethodStatus.ACTIVE)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Scored Method not found"));

        Environment environment = environmentRepository.findByIdAndStatus(newRound.getEnvironmentId(), EnvironmentStatus.ACTIVE)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Environment not found"));

        Resource resource = resourceRepository.findByIdAndIsEnable(newRound.getResourceId(), true)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Map not found"));

        if (resource.getResourceType() != ResourceType.MAP) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The resource is not a map");
        }

        if (tournament.getStatus() == TournamentStatus.COMPLETED ||
                tournament.getStatus() == TournamentStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The tournament is not available, can't create round");
        }

        if (newRound.getFinishType() == FinishType.LAP) {
            newRound.setRoundDuration(0);
        } else {
            newRound.setLapNumber(0);
        }

        newRoundValidation(newRound, tournament);
        Round round = Round.builder()
                .roundName(newRound.getRoundName())
                .roundDuration(newRound.getRoundDuration())
                .lapNumber(newRound.getLapNumber())
                .finishType(newRound.getFinishType())
                .teamLimit(newRound.getTeamLimit())
                .description(newRound.getDescription())
                .status(RoundStatus.ACTIVE)
                .isLast(newRound.isLast())
                .isLatest(true)
                .startDate(newRound.getStartDate())
                .endDate(newRound.getEndDate())
                .tournament(tournament)
                .matchType(matchType)
                .scoredMethod(scoredMethod)
                .environment(environment)
                .resource(resource)
                .build();
        roundRepository.save(round);

        updateLatestRound(tournament);
        generateMatch(round, tournament);
//        generateLeaderboard(round);
    }

    private void updateLatestRound(Tournament tournament) {
        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournament.getId()).stream()
                .sorted(Comparator.comparing(Round::getTeamLimit).reversed())
                .toList();

        if (!rounds.isEmpty()) {
            for (Round round : rounds) {
                round.setLatest(false);
            }

            Round round = rounds.get(rounds.size() - 1);
            round.setLatest(true);
            roundRepository.saveAll(rounds);
        }
    }

    private void newRoundValidation(NewRound newRound, Tournament tournament) {

        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournament.getId()).stream()
                .sorted(Comparator.comparingInt(Round::getTeamLimit).reversed()).toList();

        //Kiểm tra xem đã tạo round cuối cùng chưa
        for (Round round : rounds) {
            if (round.isLast()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The last round has been created, can't create more round");
            }
        }

        //Nếu round đang tạo là round cuối cùng thì team limit phải bằng 0
        if (newRound.isLast() && newRound.getTeamLimit() != 0) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be 0 for the last round");
        }

        //Đảm bảo team limit (số team còn lại vòng tiếp theo) luôn bé hơn số lượng team vòng trước
        //Có 2 trường hợp: trường hợp tạo round đầu tiên và trường hợp tạo round tiếp theo

        //Trường hợp tạo round đầu tiên
        if (rounds.isEmpty()) {
            if (newRound.getTeamLimit() >= tournament.getTeamNumber()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be less than the tournament team number: " + tournament.getTeamNumber());
            }
        } else {
            //Trường hợp tạo round tiếp theo
            if (newRound.getTeamLimit() >= rounds.get(rounds.size() - 1).getTeamLimit()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be less than or equal to the previous round team number: " + rounds.get(rounds.size() - 1).getTeamLimit());
            }
        }

        if (!newRound.isLast()) {
            if (newRound.getTeamLimit() <= 1) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be greater than 1");
            }
        }


        //Để tạo vòng này cần ít nhất bao nhiêu trận đấu
        //Trường hợp tạo round đầu tiên
        MatchType matchType = matchTypeRepository.findById(newRound.getMatchTypeId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match Type not found"));

        //Số trận đấu cần phải tạo
        //Trường hợp vòng đầu
        double matches = (double) tournament.getTeamNumber() / matchType.getTeamNumber();

        //Trường hợp đã có ít nhất 1 vòng
        if (!rounds.isEmpty()) {
            matches = rounds.get(rounds.size() - 1).getTeamLimit();
        }

        //Nếu số trận đấu cần tạo không phải là số nguyên thì báo lỗi
        if (matches % 1 != 0) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The number of team is invalid for this match type, please check the number of team in tournament");
        }

        //Xác định giới hạn thời gian của round
        //Trường hợp vòng đầu tiên
        Date boundaryStartDate = tournament.getStartDate();
        Date boundaryEndDate = tournament.getEndDate();

        //Trường hợp đã có ít nhất 1 vòng
        if (!rounds.isEmpty()) {
            boundaryStartDate = rounds.get(rounds.size() - 1).getEndDate();
        }

        validateTime(newRound.getStartDate(), newRound.getEndDate(), boundaryStartDate, boundaryEndDate, matches);
    }

    private void validateTime(Date start, Date end, Date boundaryStartDate, Date boundaryEndDate, double matches) {
        Calendar calendar = Calendar.getInstance();

        if (start.after(end)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "End date must be after start date.");
        }

        // Round bắt đầu sau ít nhất sau ngày hôm nay
        if (start.before(calendar.getTime()) || end.before(calendar.getTime())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Start date and end date must be after today: " + DateUtil.formatTimestamp(calendar.getTime()));
        }

        //Trường hợp vòng đầu tiên thì thời gian bắt đầu phải nằm trong khoảng thời gian hoạt động của tournament
        //Trường hợp vòng thứ 1 trở đi thì vòng bắt đầu nằm sau thời gian kết thúc vòng trước đó và nằm trước thời gian kết thúc của

        if (start.before(boundaryStartDate) || start.after(boundaryEndDate)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "The round start date is invalid, round start date must be after " +
                            DateUtil.formatTimestamp(boundaryStartDate) + " and before " +
                            DateUtil.formatTimestamp(boundaryEndDate));
        }

        if (end.after(boundaryEndDate) || end.before(boundaryStartDate)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "The round end date is invalid, round end date must be after " +
                            DateUtil.formatTimestamp(boundaryStartDate) + " and before " +
                            DateUtil.formatTimestamp(boundaryEndDate));
        }

        LocalDate localStartDate = DateUtil.convertToLocalDateTime(start).toLocalDate();
        LocalDate localEndDate = DateUtil.convertToLocalDateTime(end).toLocalDate();

        long daysBetween = ChronoUnit.DAYS.between(localStartDate, localEndDate);
        int result = (daysBetween == 0) ? 1 : (int) daysBetween;

        if (result < Math.ceil(matches / Schedule.MAX_WORKING_HOURS)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "The round schedule is invalid, the round must have at least " + matches +
                            " working hours, at least " + Math.ceil(matches / Schedule.MAX_WORKING_HOURS) + " days to create matches");
        }

    }

    @Transactional
    @Override
    public void editRound(EditRound request) {
        Round round = roundRepository.findById(request.getId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

        Tournament tournament = tournamentRepository.findById(round.getTournament().getId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Tournament not found"));

        MatchType matchType = matchTypeRepository.findByIdAndStatus(request.getMatchTypeId(), MatchTypeStatus.ACTIVE)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match Type not found"));

        ScoredMethod scoredMethod = scoredMethodRepository.findByIdAndStatus(request.getScoredMethodId(), ScoredMethodStatus.ACTIVE)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Scored Method not found"));

        Environment environment = environmentRepository.findByIdAndStatus(request.getEnvironmentId(), EnvironmentStatus.ACTIVE)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Environment not found"));

        Resource map = resourceRepository.findByIdAndIsEnable(request.getResourceId(), true)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Map not found"));

        if (map.getResourceType() != ResourceType.MAP) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Resource invalid, The resource need to be a map");
        }

        if (tournamentServiceImpl.isMatchStarted(round.getId())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Cannot edit round, there are matches in this round that have started");
        }

        request.setStartDate(DateUtil.convertToStartOfTheDay(request.getStartDate()));
        request.setEndDate(DateUtil.convertToEndOfTheDay(request.getEndDate()));

        if (request.getFinishType() == FinishType.LAP) {
            request.setRoundDuration(0);
        } else {
            request.setLapNumber(0);
        }

        editRoundValidation(request, tournament);

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setAmbiguityIgnored(true)
                .setSkipNullEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.map(request, round);

        //Đổi match type thì phải generate lại match
        boolean flag = false;
        if (round.getMatchType().getId() != request.getMatchTypeId()) {
            List<Match> matches = matchRepository.findByRoundId(request.getId());
            for (Match match : matches) {
                match.setStatus(MatchStatus.TERMINATED);
                List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());
                for (MatchTeam matchTeam : matchTeams) {
                    matchTeam.setStatus(MatchTeamStatus.TERMINATED);
                    matchTeamRepository.save(matchTeam);
                }
                matchRepository.save(match);
            }
            flag = true;
        }

        round.setMatchType(matchType);
        round.setScoredMethod(scoredMethod);
        round.setEnvironment(environment);
        round.setResource(map);

        roundRepository.save(round);
        if (flag) {
            generateMatch(round, tournament);
        }
    }

    private void editRoundValidation(EditRound editRound, Tournament tournament) {

        if (tournament.getStatus() != TournamentStatus.ACTIVE) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The tournament is completed or terminated, can't edit round");
        }

        //Find round dđể update thì chỉ tìm round status = pending và active
        List<Round> rounds = roundRepository.findValidRoundByTournamentId(tournament.getId()).stream()
                .sorted(Comparator.comparing(Round::getTeamLimit).reversed()).toList();

        //Nếu round đang tạo là round cuối cùng thì team limit phải bằng 0
        if (editRound.isLast() && editRound.getTeamLimit() != 0) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be 0 for the last round");
        }

        //Đảm bảo team limit (số team còn lại vòng tiếp theo) luôn bé hơn số lượng team vòng trước
        //Có 2 trường hợp: trường hợp tạo round đầu tiên và trường hợp tạo round tiếp theo

        //Trường hợp tạo round đầu tiên
        if (rounds.size() == 1) {
            if (editRound.getTeamLimit() >= tournament.getTeamNumber()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be less than the tournament team number: " + tournament.getTeamNumber());
            }
        } else {
            //Trường hợp tạo round tiếp theo
            if (editRound.getTeamLimit() >= rounds.get(rounds.size() - 2).getTeamLimit()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be less than or equal to the previous round team number: " + rounds.get(rounds.size() - 2).getTeamLimit());
            }
        }

        if (!editRound.isLast()) {
            if (editRound.getTeamLimit() <= 1) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be greater than 1");
            }
        }

        LocalDateTime roundStartTime = DateUtil.convertToLocalDateTime(editRound.getStartDate());

        if (roundStartTime.isBefore(DateUtil.convertToLocalDateTime(tournament.getStartDate()))) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round start date is invalid, round start date must be after tournament start date: " + DateUtil.formatTimestamp(tournament.getStartDate()));
        }

        //Để tạo vòng này cần ít nhất bao nhiêu trận đấu
        //Trường hợp tạo round đầu tiên
        MatchType matchType = matchTypeRepository.findById(editRound.getMatchTypeId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match Type not found"));

        //Số trận đấu cần phải tạo
        //Trường hợp vòng đầu
        double matches = (double) tournament.getTeamNumber() / matchType.getTeamNumber();

        //Trường hợp đã có ít nhất 1 vòng
        if (rounds.size() > 1) {
            matches = rounds.get(rounds.size() - 2).getTeamLimit();
        }

        //Nếu số trận đấu cần tạo không phải là số nguyên thì báo lỗi
        if (matches % 1 != 0) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The number of team is invalid for this match type, please check the number of team in tournament");
        }

        //Xác định giới hạn thời gian của round
        //Trường hợp vòng đầu tiên
        Date boundaryStartDate = tournament.getStartDate();
        Date boundaryEndDate = tournament.getEndDate();

        //Trường hợp đã có ít nhất 1 vòng
        if (rounds.size() > 1) {
            boundaryStartDate = rounds.get(rounds.size() - 2).getEndDate();
        }

        validateTime(editRound.getStartDate(), editRound.getEndDate(), boundaryStartDate, boundaryEndDate, matches);
    }

    @Transactional
    @Override
    public void terminateRound(Long id) {

        Round round = roundRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

        if (round.getStatus() != RoundStatus.ACTIVE) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Terminate fails, this round has been completed or terminated");
        }

        //Trường hợp round đã có trận đấu diễn ra
        if (tournamentServiceImpl.isMatchStarted(round.getTournament().getId())) {
            //Terminate các round, match chưa hoàn thành
            tournamentServiceImpl.terminateRoundByTournamentId(id);
        }

        round.setStatus(RoundStatus.TERMINATED);
        round.setLatest(false);
        roundRepository.save(round);
        updateLatestRound(round.getTournament());
    }

    private void generateMatch(Round round, Tournament tournament) {
        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournament.getId()).stream()
                .sorted(Comparator.comparing(Round::getTeamLimit).reversed()).toList();

        MatchType matchType = round.getMatchType();
        double matchDuration = matchType.getMatchDuration() * 60;

        int numberOfPlayers = matchType.getPlayerNumber(); // số người mỗi team
        int numberOfTeams = matchType.getTeamNumber(); // số team

        //Số trận cần tạo
        double numberOfMatches;
        //Nếu là round đầu tiên thì số lượng match = số lượng team của tournament / số team trong 1 trận
        if (rounds.size() == 1) {
            numberOfMatches = (double) tournament.getTeamNumber() / numberOfTeams;
        } else {
            //Nếu là round thứ 2 trở đi thì số lượng match = teamLimit của round trước / số team trong 1 trận
            numberOfMatches = (double) rounds.get(rounds.size() - 2).getTeamLimit() / numberOfTeams;
        }

        if (numberOfMatches % 1 != 0) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The number of team is invalid for this match type, please check the number of team in tournament");
        }

        LocalDateTime startTime = DateUtil.convertToLocalDateTime(round.getStartDate()).withHour(Schedule.WORKING_HOURS_START);
        LocalDateTime endTime = DateUtil.convertToLocalDateTime(round.getEndDate()).withHour(Schedule.WORKING_HOURS_END);

        LocalTime workStart = LocalTime.of(Schedule.WORKING_HOURS_START, 0);
        LocalTime workEnd = LocalTime.of(Schedule.WORKING_HOURS_END, 0);

        //Build string matchName
        StringBuilder stringBuilder = new StringBuilder();
        String seasonPrefix = GenerateCode.seasonPrefix(DateUtil.convertToLocalDateTime(tournament.getStartDate()));
        String matchTypePrefix = matchType.getMatchTypeCode();

        //Tạo từng match
        while (startTime.isBefore(endTime) && numberOfMatches > 0) {
            LocalTime currentTime = startTime.toLocalTime();
            // Nếu trong khung giờ làm việc (8:00 - 17:00), tăng biến đếm
            if (!currentTime.isBefore(workStart) && currentTime.isBefore(workEnd) && currentTime.getHour() != Schedule.LUNCH_BREAK_START) {

                stringBuilder.setLength(0);
                stringBuilder.append(matchType.getMatchTypeName());

                String name = stringBuilder.toString();

                //generate match code
                String matchCode;
                Optional<Match> isDuplicate;
                do {
                    //Ghép lại thành match code
                    stringBuilder.setLength(0);
                    matchCode = stringBuilder
                            .append(seasonPrefix)
                            .append("_")
                            .append(matchTypePrefix)
                            .append("_")
                            .append(GenerateCode.generateMatchCode())
                            .toString();

                    isDuplicate = matchRepository.findByMatchCode(matchCode);
                } while (isDuplicate.isPresent());

                Match match = Match.builder()
                        .matchName(name)
                        .matchCode(matchCode)
                        .timeStart(DateUtil.convertToDate(startTime))
                        .timeEnd(DateUtil.convertToDate(startTime.plusMinutes((long) matchDuration)))
                        .status(MatchStatus.PENDING)
                        .round(round)
                        .build();

                matchRepository.save(match);

                startTime = startTime.plusHours(Schedule.SLOT_DURATION);
                numberOfMatches--;
            } else {
                if (currentTime.getHour() == Schedule.WORKING_HOURS_END) {
                    startTime = startTime.plusDays(1).withHour(Schedule.WORKING_HOURS_START);
                } else {
                    startTime = startTime.plusHours(Schedule.SLOT_DURATION);
                }
            }
        }

        generateMatchTeam(round, numberOfPlayers, numberOfTeams);

    }

    private void generateMatchTeam(Round round, int playersPerTeam, int teamsPerMatch) {

        int totalMatchTeam = playersPerTeam * teamsPerMatch;

        List<Match> matches = matchRepository.findByRoundId(round.getId()).stream()
                .filter(match -> match.getStatus() == MatchStatus.PENDING).toList();

        //Tạo sẵn matchTeam cho từng match
        for (Match match : matches) {
            for (int i = 0; i < totalMatchTeam; i++) {
                MatchTeam matchTeam = new MatchTeam();
                matchTeam.setMatch(match);
                matchTeam.setStatus(MatchTeamStatus.UNASSIGNED);
                matchTeam.setScore(0);
                matchTeam.setAttempt(0);
                matchTeamRepository.save(matchTeam);
            }
        }

        injectTeamToMatchTeam(round.getTournament().getId());

    }

    public void injectTeamToMatchTeam(Long tournamentId) {

        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournamentId).stream()
                .sorted(Comparator.comparing(Round::getTeamLimit).reversed()).toList();

        //Round đầu tiên
        Round round = rounds.get(0);
        List<Leaderboard> leaderboards = new ArrayList<>();
        if (rounds.size() > 1) {
            //round Status = COMPLETED
            if (round.getStatus() == RoundStatus.COMPLETED) {
                // leaderboard đã đc chốt
                leaderboards = leaderboardRepository.findByRoundId(round.getId()).stream()
                        .sorted(Comparator.comparing(Leaderboard::getRanking))
                        .limit(round.getTeamLimit())
                        .toList();
                //Round thứ 2 trở đi
                round = rounds.get(rounds.size() - 2);

            } else {
                return;
            }
        }

        List<Team> teams = tournamentTeamRepository.findByTournamentId(tournamentId).stream()
                .map(TournamentTeam::getTeam)
                .distinct().toList();

        if (!leaderboards.isEmpty()) {
            //Lấy danh sách team của round trước đó
            teams = leaderboards.stream()
                    .map(Leaderboard::getTeam)
                    .toList();
        }

        if (teams.isEmpty()) {
            return;
        }

        //Lấy danh sách matchTeam của round
        List<Match> matches = matchRepository.findByRoundId(round.getId()).stream().filter(match -> match.getStatus() == MatchStatus.PENDING).toList();
        List<MatchTeam> matchTeams = new ArrayList<>();
        for (Match match : matches) {
            matchTeams.addAll(matchTeamRepository.findByMatchId(match.getId()));
        }

        //Lấy danh sách matchTeam chưa được gán team
        List<MatchTeam> unassignedMatchTeams = matchTeams.stream()
                .filter(matchTeam -> matchTeam.getStatus() == MatchTeamStatus.UNASSIGNED)
                .sorted(Comparator.comparingLong(mt -> mt.getMatch().getId()))
                .toList();

        if (unassignedMatchTeams.isEmpty()) {
            return;
        }

        List<Team> assignedTeam = new ArrayList<>();

        //Lấy danh sách team đã được gán vào matchTeam
        for (Team t : teams) {
            List<Team> team = matchTeams.stream()
                    .map(MatchTeam -> {
                        if (MatchTeam.getTeam() == null) {
                            return null;
                        }

                        return MatchTeam.getTeam().getId() == t.getId() ? t : null;
                    })
                    .distinct().toList();

            if (!team.isEmpty()) {
                assignedTeam.addAll(team);
            }
        }

        //Lấy danh sách team chưa được gán vào matchTeam
        List<Team> unassignedTeams = new ArrayList<>(teams.stream()
                .filter(team -> !assignedTeam.contains(team))
                .toList());

        //Gán team vào matchTeam
        int numberOfTeamInMatchTeam = round.getMatchType().getPlayerNumber();

        Iterator<Team> iterator = unassignedTeams.iterator();
        int i = 0;
        int index = 0;
        outer:
        while (iterator.hasNext()) {
            Team team = iterator.next();

            if (team == null) {
                break;
            }

            do {

                if (index == numberOfTeamInMatchTeam) {
                    iterator.remove();
                    index = 0;
                    break;
                }

                unassignedMatchTeams.get(i).setTeam(team);
                unassignedMatchTeams.get(i).setStatus(MatchTeamStatus.ASSIGNED);
                index++;
                i++;
            } while (i < unassignedMatchTeams.size());
        }
        matchTeamRepository.saveAll(unassignedMatchTeams);

        //Gán tên team vào match
        unassignedMatchTeams.stream()
                .filter(mt -> mt.getTeam() != null)
                .collect(Collectors.groupingBy(MatchTeam::getMatch, Collectors.mapping(MatchTeam::getTeam, Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Team::getId))),
                        ArrayList::new
                ))))
                .entrySet().stream()
                .map(entry -> {
                    Match match = entry.getKey();
                    List<Team> teamList = entry.getValue();

                    StringBuilder matchName = new StringBuilder(match.getMatchName()).append(" - ");
                    for (int j = 0; j < teamList.size(); j++) {
                        matchName.append(teamList.get(j).getTeamName());
                        if (j < teamList.size() - 1) matchName.append(", ");
                    }
                    match.setMatchName(matchName.toString());
                    return match;
                }).toList();

        matchTeamRepository.saveAll(unassignedMatchTeams);
    }

    @Override
    public RoundResponse findRoundByRoundId(Long id) {

        Round round = roundRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

        modelMapper.getConfiguration().setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setAmbiguityIgnored(true)
                .setSkipNullEnabled(false)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        RoundResponse roundResponse = modelMapper.map(round, RoundResponse.class);

        roundResponse.setStartDate(DateUtil.formatTimestamp(round.getStartDate()));
        roundResponse.setEndDate(DateUtil.formatTimestamp(round.getEndDate()));
        roundResponse.setCreateDate(DateUtil.formatTimestamp(round.getCreatedDate()));
        roundResponse.setMatchTypeName(round.getMatchType().getMatchTypeName());
        roundResponse.setTournamentId(round.getTournament().getId());
        roundResponse.setScoredMethodId(round.getScoredMethod().getId());
        roundResponse.setEnvironmentId(round.getEnvironment().getId());
        roundResponse.setMatchTypeId(round.getMatchType().getId());
        roundResponse.setMapId(round.getResource().getId());

        return roundResponse;

    }

    @Override
    public List<RoundResponse> findRoundByTournamentId(Long id) {
        // by admin, organizer
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Tournament not found"));

        List<Round> rounds = roundRepository.findByTournamentId(tournament.getId());
        List<RoundResponse> roundResponses = new ArrayList<>();
        rounds.forEach(round -> {
            RoundResponse roundResponse = findRoundByRoundId(round.getId());
            roundResponses.add(roundResponse);
        });

        return roundResponses;
    }

    @Override
    public ListRoundResponse findAllRounds(int pageNo, int pageSize, RoundSort sortBy, String keyword) {

        Sort sort = Sort.by(sortBy.getField()).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Specification<Round> spec = Specification.where(RoundSpecification.hasRoundName(keyword));

        Page<Round> roundPage = roundRepository.findAll(spec, pageable);

        List<RoundResponse> roundResponses = new ArrayList<>();
        roundPage.getContent().forEach(round -> {
            RoundResponse roundResponse = findRoundByRoundId(round.getId());
            roundResponses.add(roundResponse);
        });

        ListRoundResponse listRoundResponses = new ListRoundResponse();
        listRoundResponses.setTotalPages(roundPage.getTotalPages());
        listRoundResponses.setTotalElements(roundPage.getTotalElements());
        listRoundResponses.setPageNo(roundPage.getNumber());
        listRoundResponses.setPageSize(roundPage.getSize());
        listRoundResponses.setLast(roundPage.isLast());
        listRoundResponses.setContent(roundResponses);

        return listRoundResponses;
    }

    //second, minute, hour, day, month, year
    //* = every
//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(cron = "1 0 0 * * ?")
    @Transactional
    public void checkIfRoundEnd() {
        logger.info("Detecting round end task is running");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();

        //Kiểm tra xem có vòng sẽ kết thúc ko
        Optional<Round> roundEnd = roundRepository.findByStatusAndEndDateBefore(RoundStatus.ACTIVE, date);
        if (roundEnd.isPresent()) {
            logger.info("Found a round that reach end date");
            Tournament tournament = roundEnd.get().getTournament();
            if (tournament.getStatus() == TournamentStatus.ACTIVE) {
                roundEnd.get().setStatus(RoundStatus.COMPLETED);
                roundRepository.save(roundEnd.get());
                logger.info("Change round status to completed. Round id: {}", roundEnd.get().getId());
            }

            injectTeamToMatchTeam(tournament.getId());
        }

        logger.info("Detecting round end task is completed");
    }

    @Override
    public GetRoundsByAccountResponse getRoundsByAccountId(UUID accountId, int pageNo, int pageSize, RoundSort sortBy, String keyword) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Round> spec = Specification.where(RoundSpecification.hasKeyword(keyword));

        Page<Round> roundsPage = roundRepository.findAll(spec, pageable);
        List<Round> rounds = roundsPage.getContent();

        List<GetPlayerRoundResponse> roundResponses = rounds.stream()
                .map(round -> new GetPlayerRoundResponse(
                        round.getId(),
                        round.getRoundName(),
                        round.getTeamLimit(),
                        round.isLast(),
                        round.getDescription(),
                        round.getStatus(),
                        round.getStartDate() != null ? round.getStartDate().toString() : null,
                        round.getEndDate() != null ? round.getEndDate().toString() : null,
                        round.getCreatedDate() != null ? round.getCreatedDate().toString() : null,
                        round.getTournament() != null ? round.getTournament().getId() : null,
                        round.getTournament() != null ? round.getTournament().getTournamentName() : null,
                        round.getScoredMethod() != null ? round.getScoredMethod().getId() : null,
                        round.getEnvironment() != null ? round.getEnvironment().getId() : null,
                        round.getMatchType() != null ? round.getMatchType().getId() : null,
                        round.getMatchType() != null ? round.getMatchType().getMatchTypeName() : null,
                        round.getResource() != null ? round.getResource().getId() : null,
                        round.getMatchType() != null ? round.getFinishType() : null
                ))
                .collect(toList());

        return GetRoundsByAccountResponse.builder()
                .rounds(roundResponses)
                .totalPages(roundsPage.getTotalPages())
                .totalElements(roundsPage.getTotalElements())
                .pageNo(roundsPage.getNumber())
                .pageSize(roundsPage.getSize())
                .last(roundsPage.isLast())
                .build();
    }

    @Override
    public ListRoundResponse findAllRoundsByDate(int pageNo, int pageSize, RoundSort sortBy, String keyword, LocalDateTime start, LocalDateTime end) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Specification<Round> spec = Specification
                .where(RoundSpecification.hasKeyword(keyword))
                .and(RoundSpecification.betweenStartAndEndDate(start, end)
                        .and(RoundSpecification.hasStatus(RoundStatus.ACTIVE))
                );

        Page<Round> roundPage = roundRepository.findAll(spec, pageable);
        List<RoundResponse> roundResponses = new ArrayList<>();
        roundPage.getContent().forEach(round -> {
            if (round.getTournament().getStatus() == TournamentStatus.ACTIVE) {
                RoundResponse roundResponse = findRoundByRoundId(round.getId());
                roundResponses.add(roundResponse);
            }
        });

        ListRoundResponse listRoundResponses = new ListRoundResponse();
        listRoundResponses.setTotalPages(roundPage.getTotalPages());
        listRoundResponses.setTotalElements(roundPage.getTotalElements());
        listRoundResponses.setPageNo(roundPage.getNumber());
        listRoundResponses.setPageSize(roundPage.getSize());
        listRoundResponses.setLast(roundPage.isLast());
        listRoundResponses.setContent(roundResponses);

        return listRoundResponses;
    }

    @Override
    public void injectTeamToTournament(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Tournament not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Team not found"));

        if (team.getStatus() != TeamStatus.ACTIVE) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Team is not active");
        }
        List<Team> teams = tournamentTeamRepository.findByTournamentId(tournamentId).stream()
                .map(TournamentTeam::getTeam)
                .distinct()
                .toList();

        if (tournament.getTeamNumber() <= teams.size()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The tournament has reached the maximum number of teams");
        }

        TournamentTeam tournamentTeam = new TournamentTeam();
        tournamentTeam.setTournament(tournament);
        tournamentTeam.setTeam(team);
        tournamentTeamRepository.save(tournamentTeam);
        injectTeamToMatchTeam(tournamentId);
    }

    public void generateLeaderboard(Round round) {

//        List<Team> teams = teamRepository.getTeamByTournamentIdAndStatus(round.getTournament().getId(), TeamStatus.ACTIVE);
        List<Team> teams = null;
        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(round.getTournament().getId()).stream()
                .sorted(Comparator.comparing(Round::getTeamLimit).reversed()).toList();
        //Trường hợp round đang tạo là round đầu tiên
        if (rounds.size() == 1) {
            //Tạo leaderboard cho tất cả các team tham gia tournament
            for (Team team : teams) {
                Leaderboard leaderboard = new Leaderboard();
                leaderboard.setRound(round);
                leaderboard.setTeam(team);
                leaderboard.setTeamScore(0);
                leaderboard.setRanking(0);
                leaderboardRepository.save(leaderboard);
            }
        }
        //Trường hợp round đang tạo không phải là round đầu tiên
        else {
            Round previousRound = rounds.get(rounds.size() - 2);
            //Trường hợp round trước đó chưa hoàn thành
            if (previousRound.getStatus() != RoundStatus.COMPLETED) {
                return;
            }

            List<Leaderboard> leaderboards = leaderboardRepository.findByRoundId(previousRound.getId()).stream()
                    .filter(leaderboard -> leaderboard.getTeam().getStatus() == TeamStatus.ACTIVE && !leaderboard.getTeam().isDisqualified())
                    .sorted(Comparator.comparingInt(Leaderboard::getRanking)) // sắp xếp tăng dần theo ranking
                    .limit(6) // chỉ lấy top 6
                    .toList();

            //leaderboards empty nghĩa là round 1 chưa hoàn thành, chưa có dữ liệu leaderboard
            if (leaderboards.isEmpty()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The round is not completed yet, please check the leaderboard");
            }

            for (Leaderboard leaderboard : leaderboards) {
                Leaderboard newLeaderboard = new Leaderboard();
                newLeaderboard.setRound(round);
                newLeaderboard.setTeam(leaderboard.getTeam());
                newLeaderboard.setTeamScore(0);
                newLeaderboard.setRanking(0);
                leaderboardRepository.save(newLeaderboard);
            }
        }


    }

}
