package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.GenerateCode;
import com.sep490.dasrsbackend.Util.RoundSpecification;
import com.sep490.dasrsbackend.Util.Schedule;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.*;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.TournamentRuleException;
import com.sep490.dasrsbackend.model.payload.request.EditRound;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.model.payload.response.GetPlayerRoundResponse;
import com.sep490.dasrsbackend.model.payload.response.GetRoundsByAccountResponse;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Transactional
    @Override
    public void newRound(NewRound newRound) {

        newRound.setStartDate(DateUtil.convertUTCtoICT(newRound.getStartDate()));
        newRound.setEndDate(DateUtil.convertUTCtoICT(newRound.getEndDate()));

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

        newRoundValidation(newRound, tournament);
        Round round = Round.builder()
                .roundName(newRound.getRoundName())
                .teamLimit(newRound.getTeamLimit())
                .description(newRound.getDescription())
                .status(RoundStatus.PENDING)
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
    }

    private void updateLatestRound(Tournament tournament) {
        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournament.getId());
        if (!rounds.isEmpty()) {
            for (Round round : rounds) {
                round.setLatest(false);
                roundRepository.save(round);
            }

            Round round = rounds.get(rounds.size() - 1);
            round.setLatest(true);
            roundRepository.save(round);
        }
    }

    private void newRoundValidation(NewRound newRound, Tournament tournament) {

        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournament.getId());

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


        LocalDateTime roundStartTime = DateUtil.convertToLocalDateTime(newRound.getStartDate());

        if (roundStartTime.isBefore(DateUtil.convertToLocalDateTime(tournament.getStartDate()))) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round start date is invalid, round start date must be after tournament start date: " + DateUtil.formatTimestamp(tournament.getStartDate()));
        }

        //Để tạo vòng này cần ít nhất bao nhiêu trận đấu
        //Trường hợp tạo round đầu tiên
        double currMatches = tournament.getTeamNumber() * Schedule.SLOT_DURATION;

        //Trường hợp tạo các round tiếp theo
        if (!rounds.isEmpty()) {
            currMatches = rounds.get(rounds.size() - 1).getTeamLimit() * Schedule.SLOT_DURATION;
        }

        //Trường hợp vòng đầu tiên
        LocalDateTime tStart = DateUtil.convertToLocalDateTime(tournament.getStartDate());
        LocalDateTime tEnd = DateUtil.convertToLocalDateTime(tournament.getEndDate());

        //Trường hợp đã có ít nhất 1 vòng
        if (!rounds.isEmpty()) {
            tStart = DateUtil.convertToLocalDateTime(rounds.get(rounds.size() - 1).getEndDate()).plusDays(1).withHour(Schedule.WORKING_HOURS_START).withMinute(0).withSecond(0).withNano(0);
        }

        if (tStart.getMinute() > 0 || tStart.getSecond() > 0 || tStart.getNano() > 0) {
            tStart = tStart.plusHours(1).truncatedTo(ChronoUnit.HOURS);
        }

        if (tEnd.getMinute() > 0 || tEnd.getSecond() > 0 || tEnd.getNano() > 0) {
            tEnd = tEnd.truncatedTo(ChronoUnit.HOURS);
        }

        LocalDateTime rStart = DateUtil.convertToLocalDateTime(newRound.getStartDate());
        LocalDateTime rEnd = DateUtil.convertToLocalDateTime(newRound.getEndDate());

        newRound.setStartDate(DateUtil.convertToDate((rStart.withHour(0).withMinute(0).withSecond(0).withNano(0))));
        newRound.setEndDate(DateUtil.convertToDate((rEnd.withHour(23).withMinute(23).withSecond(23).withNano(23))));

        //Ngaỳ tạo vòng mới phải cách là hôm sau ngày hôm nay
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date today = calendar.getTime();
        if (newRound.getStartDate().before(today)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round start date is invalid, round start date must be after today: " + DateUtil.formatTimestamp(today));
        }

        validateTime(newRound.getStartDate(), newRound.getEndDate(), tStart, currMatches, tEnd);
    }

    private void validateTime(Date rStart, Date rEnd, LocalDateTime tStart, double currMatches, LocalDateTime tEnd) {

        //Thời gian bắt đầu sớm nhất của vòng này sẽ bằng
        //thời gian kết thúc của vòng trước đó
        //hoặc thời gian bắt đầu của tournament
        LocalDateTime earliestStartTime = tStart.withHour(Schedule.WORKING_HOURS_START);

        //Thời gian kết thúc sớm nhất của vòng này =
        //thời gian bắt đầu sớm nhất vòng này + số thời gian cần thiết để tạo trận đấu cho vòng này
        LocalDateTime earliestEndTime = plusAtleastMatchHours(earliestStartTime, currMatches);

        //Thời gian kết thúc trễ nhất của vòng này sẽ bằng thời gian kết thúc của tournament
        LocalDateTime latestEndTime = tEnd.withHour(23).withMinute(59).withSecond(59);
        //Thời gian bắt đầu trễ nhất của vòng này =
        //thời gian kết thúc trễ nhất của vòng này - số thời gian cần thiết để tạo trận đấu cho vòng này
        LocalDateTime latestStartTime = minusAtLeastMatchHours(latestEndTime.withHour(Schedule.WORKING_HOURS_END), currMatches);

        LocalDateTime localNewRoundStartTime = DateUtil.convertToLocalDateTime(rStart).withHour(Schedule.WORKING_HOURS_START);
        LocalDateTime localNewRoundEndTime = DateUtil.convertToLocalDateTime(rEnd).withHour(23).withMinute(59).withSecond(59);

        if (localNewRoundStartTime.isBefore(earliestStartTime) || localNewRoundStartTime.isAfter(latestStartTime)) {
            Map<String, String> response = getDataResponse(earliestStartTime, latestEndTime, localNewRoundStartTime.isBefore(earliestStartTime), localNewRoundStartTime);

            throw new TournamentRuleException(HttpStatus.BAD_REQUEST,
                    "The round start date is invalid, round start date is between " +
                            DateUtil.formatTimestamp(DateUtil.convertToDate(earliestStartTime)) + " and " +
                            DateUtil.formatTimestamp(DateUtil.convertToDate(latestStartTime)),
                    response);
        }

        if (localNewRoundEndTime.isBefore(earliestEndTime) || localNewRoundEndTime.isAfter(latestEndTime)) {
            Map<String, String> response = getDataResponse(earliestEndTime, latestEndTime, localNewRoundStartTime.isBefore(earliestEndTime), localNewRoundEndTime);

            throw new TournamentRuleException(HttpStatus.BAD_REQUEST,
                    "The round end date is invalid, round end date is between " +
                            DateUtil.formatTimestamp(DateUtil.convertToDate(earliestEndTime)) + " and " +
                            DateUtil.formatTimestamp(DateUtil.convertToDate(latestEndTime)),
                    response);
        }

        if (!validateWorkingHours(localNewRoundStartTime, localNewRoundEndTime, (int) currMatches)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "The round schedule is invalid, the round must have at least " + currMatches +
                            " working hours, at least " + Math.ceil(currMatches / Schedule.MAX_WORKING_HOURS) + " days  to create matches");
        }
    }

    private Map<String, String> getDataResponse(LocalDateTime earliestStartTime, LocalDateTime latestEndTime, boolean before, LocalDateTime localNewRoundStartTime) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("startDate", DateUtil.formatTimestamp(DateUtil.convertToDate(earliestStartTime)));
        response.put("endDate", DateUtil.formatTimestamp(DateUtil.convertToDate(latestEndTime)));
        if (!before) {
            response.put("type", "over");
        } else {
            response.put("type", "under");
        }
        return response;
    }

    private LocalDateTime minusAtLeastMatchHours(LocalDateTime roundStartTime, double totalHours) {
        for (int i = 0; i < totalHours; i++) {
            roundStartTime = roundStartTime.minusHours(Schedule.SLOT_DURATION);
            if (roundStartTime.getHour() == Schedule.LUNCH_BREAK_END) {
                roundStartTime = roundStartTime.withHour(Schedule.LUNCH_BREAK_START);
            }
            if (roundStartTime.getHour() == Schedule.WORKING_HOURS_START) {
                roundStartTime = roundStartTime.minusDays(1).withHour(Schedule.WORKING_HOURS_END);
            }
        }
        return roundStartTime;
    }

    private LocalDateTime plusAtleastMatchHours(LocalDateTime roundEndTime, double totalHours) {
        for (int i = 0; i < totalHours; i++) {
            roundEndTime = roundEndTime.plusHours(Schedule.SLOT_DURATION);
            if (isLunchBreak(roundEndTime)) {
                roundEndTime = roundEndTime.withHour(Schedule.LUNCH_BREAK_END);
            }
            if (roundEndTime.getHour() > Schedule.WORKING_HOURS_END) {
                roundEndTime = roundEndTime.plusDays(1).withHour(Schedule.WORKING_HOURS_START);
            }
        }
        return roundEndTime;
    }

    private boolean isLunchBreak(LocalDateTime dateTime) {
        return dateTime.getHour() >= Schedule.LUNCH_BREAK_START && dateTime.getHour() < Schedule.LUNCH_BREAK_END;
    }

    public static boolean validateWorkingHours(LocalDateTime startTime, LocalDateTime endTime, int minHours) {

        if (startTime.isAfter(endTime)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round start date is invalid, round start date must be before the round end date");
        }

        LocalTime workStart = LocalTime.of(Schedule.WORKING_HOURS_START, 0);
        LocalTime workEnd = LocalTime.of(Schedule.WORKING_HOURS_END, 0);

        // Làm tròn startTime lên giờ tiếp theo
        if (startTime.getMinute() > 0 || startTime.getSecond() > 0 || startTime.getNano() > 0) {
            startTime = startTime.plusHours(1).truncatedTo(ChronoUnit.HOURS);
        }

        // Làm tròn endTime xuống giờ trước đó
        if (endTime.getMinute() > 0 || endTime.getSecond() > 0 || endTime.getNano() > 0) {
            endTime = endTime.truncatedTo(ChronoUnit.HOURS);
        }

        int flag = 0;

        // Lặp qua từng giờ từ startTime đến endTime
        while (startTime.isBefore(endTime)) {
            LocalTime currentTime = startTime.toLocalTime();
            // Nếu trong khung giờ làm việc (8:00 - 17:00), tăng biến đếm
            if (!currentTime.isBefore(workStart) && currentTime.isBefore(workEnd) && currentTime.getHour() != Schedule.LUNCH_BREAK_START) {
                flag++;
                startTime = startTime.plusHours(Schedule.SLOT_DURATION);
            } else {
                if (currentTime.getHour() == Schedule.WORKING_HOURS_END) {
                    startTime = startTime.plusDays(1).withHour(Schedule.WORKING_HOURS_START);
                } else {
                    startTime = startTime.plusHours(Schedule.SLOT_DURATION);
                }
            }
        }

        if (flag < minHours) {
            return false;
        }
        return true;
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
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The resource is not a map");
        }

        if (tournament.getStatus() == TournamentStatus.COMPLETED ||
                tournament.getStatus() == TournamentStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The tournament is not available, can't create round");
        }

        if (round.getStatus() != RoundStatus.PENDING) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, can only update a pending round");
        }

        if (!round.isLatest()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, can only update the latest round");
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
            terminatedAllMatch(request.getId());
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

    private void terminatedAllMatch(Long id) {
        List<Match> matches = matchRepository.findByRoundId(id);
        for (Match match : matches) {
            if (match.getStatus() == MatchStatus.PENDING) {
                match.setStatus(MatchStatus.CANCELLED);
                matchRepository.save(match);
            }
        }
    }

    private void editRoundValidation(EditRound editRound, Tournament tournament) {

        //Find round dđể update thì chỉ tìm round status = pending và active
        List<Round> rounds = roundRepository.findValidRoundByTournamentId(tournament.getId());

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
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be less than or equal to the previous round team number: " + rounds.get(rounds.size() - 1).getTeamLimit());
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
        double currMatches = tournament.getTeamNumber() * Schedule.SLOT_DURATION;

        //Trường hợp tạo các round tiếp theo
        if (rounds.size() > 1) {
            currMatches = rounds.get(rounds.size() - 2).getTeamLimit() * Schedule.SLOT_DURATION;
        }

        //Trường hợp vòng đầu tiên
        LocalDateTime tStart = DateUtil.convertToLocalDateTime(tournament.getStartDate());
        LocalDateTime tEnd = DateUtil.convertToLocalDateTime(tournament.getEndDate());

        //Trường hợp đã có ít nhất 1 vòng
        if (rounds.size() > 1) {
            tStart = DateUtil.convertToLocalDateTime(rounds.get(rounds.size() - 2).getEndDate()).plusDays(1).withHour(Schedule.WORKING_HOURS_START).withMinute(0).withSecond(0).withNano(0);
        }

        if (tStart.getMinute() > 0 || tStart.getSecond() > 0 || tStart.getNano() > 0) {
            tStart = tStart.plusHours(1).truncatedTo(ChronoUnit.HOURS);
        }

        if (tEnd.getMinute() > 0 || tEnd.getSecond() > 0 || tEnd.getNano() > 0) {
            tEnd = tEnd.truncatedTo(ChronoUnit.HOURS);
        }

        LocalDateTime rStart = DateUtil.convertToLocalDateTime(editRound.getStartDate());
        LocalDateTime rEnd = DateUtil.convertToLocalDateTime(editRound.getEndDate());

        editRound.setStartDate(DateUtil.convertToDate((rStart.withHour(0).withMinute(0).withSecond(0).withNano(0))));
        editRound.setEndDate(DateUtil.convertToDate((rEnd.withHour(23).withMinute(23).withSecond(23).withNano(23))));

        validateTime(editRound.getStartDate(), editRound.getEndDate(), tStart, currMatches, tEnd);
    }

    @Override
    public void changeRoundStatus(Long id, RoundStatus status) {

        Round round = roundRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

        if (status != RoundStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, this method can only terminate a round");
        } else {
            terminatedAllMatch(id);
        }
        round.setStatus(status);
        roundRepository.save(round);
    }

    private void generateMatch(Round round, Tournament tournament) {

        MatchType matchType = round.getMatchType();

        //Đưa team vào randomTeam
        double matchDuration = matchType.getMatchDuration() * 60;
        Map<Integer, Team> randomTeam = new HashMap<>();

        //Tạo biến đại diện cho số đội sẽ tham gia round
        //Truường hợp round đang tạo là round đầu tiên
        List<Team> teams = teamRepository.getTeamByTournamentIdAndStatus(tournament.getId(), TeamStatus.ACTIVE);
        int teamRemains = teams.size();

        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournament.getId());

        //Trường hợp round đang tạo không phải là round đầu tiên

        //Trường hợp round đang tạo là round đầu tiên
        if (rounds.size() == 1) {
            int flag = teamRemains;
            for (Team team : teams) {
                randomTeam.put(flag--, team);
            }
        } else { //Trường hợp round đang tạo không phải là round đầu tiên
            Round previousRound = rounds.get(rounds.size() - 2);

            //Trường hợp round trước đó chưa hoàn thành
            //Chưa generate match đc
            if (!previousRound.isLatest() || previousRound.getStatus() != RoundStatus.COMPLETED) {
                return;
            }

            teamRemains = previousRound.getTeamLimit();

            List<Leaderboard> leaderboards = leaderboardRepository
                    .findTopNLeaderboard(previousRound.getId()).stream()
                    .filter(leaderboard -> leaderboard.getTeam().getStatus() == TeamStatus.ACTIVE && !leaderboard.getTeam().isDisqualified())
                    .limit(teamRemains)
                    .toList();

            //leaderboards empty nghĩa là round 1 chưa hoàn thành, chưa có dữ liệu leaderboard
            if (leaderboards.isEmpty()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The round is not completed yet, please check the leaderboard");
            }

            for (Leaderboard leaderboard : leaderboards) {
                randomTeam.put(teamRemains--, leaderboard.getTeam());
            }
        }

        if (randomTeam.isEmpty()) {
            throw new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error. Please check the team list");
        }

        LocalDateTime startTime = DateUtil.convertToLocalDateTime(round.getStartDate()).withHour(Schedule.WORKING_HOURS_START);
        LocalDateTime endTime = DateUtil.convertToLocalDateTime(round.getEndDate()).withHour(Schedule.WORKING_HOURS_END);

        LocalTime workStart = LocalTime.of(Schedule.WORKING_HOURS_START, 0);
        LocalTime workEnd = LocalTime.of(Schedule.WORKING_HOURS_END, 0);

        StringBuilder stringBuilder = new StringBuilder();
        String seasonPrefix = GenerateCode.seasonPrefix(DateUtil.convertToLocalDateTime(tournament.getStartDate()));
        String matchTypePrefix = matchType.getMatchTypeCode();

        while (startTime.isBefore(endTime) && !randomTeam.isEmpty()) {
            LocalTime currentTime = startTime.toLocalTime();
            // Nếu trong khung giờ làm việc (8:00 - 17:00), tăng biến đếm
            if (!currentTime.isBefore(workStart) && currentTime.isBefore(workEnd) && currentTime.getHour() != Schedule.LUNCH_BREAK_START) {

                //Bóc tách match type code để lấy số đội mỗi trận đấu và số ng tham gia mỗi đội

                //MatchTypeCode = SP/MP + số người mỗi đội + số đội tham gia
                //Ví dụ match type code: "11P2T"
                String[] parts = matchTypePrefix.split("P"); // [2P, 11T]


                if (parts.length != 2) {
                    throw new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "Match type code is invalid, please contact the administrator");
                }

                String prefix = parts[0]; // 2P
                String teamCountStr = parts[1]; // 11T

                // Tách số người mỗi đội từ chuỗi prefix (vd: 2P11T)
                Matcher matcher = Pattern.compile("(\\d+)P(\\d+)T").matcher(prefix);
                if (!matcher.matches()) {
                    throw new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "Match type code is invalid, please contact the administrator");
                }

                int playersPerTeam = Integer.parseInt(matcher.group(1)); // số người mỗi team
                int teamCount = Integer.parseInt(matcher.group(2)); // số team

                isPlayersPerTeamValid(round.getTournament().getId(), playersPerTeam);

                if (randomTeam.size() % teamCount != 0) {
                    throw new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "The number of teams is not suitable for this match type, please change the match type");
                }

                //Tạo match name
                //prefix 1
                stringBuilder.setLength(0);
                String name = stringBuilder
                        .append(matchType.getMatchTypeName())
                        .append(" - ")
                        .toString();

                //tạo biến lưu lại team danh sách team đã duùng để tạo trận đấu
                List<Team> hasAssigned = new ArrayList<>();

                //Chọn đội tham gia trận đấu ngẫu nhiên
                //prefix 2
                for (int i = 0; i < teamCount; i++) {
                    Team team = randomTeam.get(teamRemains);
                    randomTeam.remove(teamRemains--);
                    if (i >= 1) {
                        name = stringBuilder.append(", ").toString();
                    }
                    name = stringBuilder.append(team.getTeamName()).toString();
                    hasAssigned.add(team);
                }

                //Ghép lại thành match code
                stringBuilder.setLength(0);
                String matchCode = stringBuilder
                        .append(seasonPrefix)
                        .append("_")
                        .append(matchTypePrefix)
                        .append("_")
                        .toString();

                Match match = Match.builder()
                        .matchName(name)
                        .matchCode(matchCode)
                        .matchScore(0)
                        .timeStart(DateUtil.convertToDate(startTime))
                        .timeEnd(DateUtil.convertToDate(startTime.plusMinutes((long) matchDuration)))
                        .status(MatchStatus.PENDING)
                        .round(round)
                        .build();

                matchRepository.save(match);
                generateMatchTeam(match, hasAssigned, playersPerTeam);

                startTime = startTime.plusHours(Schedule.SLOT_DURATION);
            } else {
                if (currentTime.getHour() == Schedule.WORKING_HOURS_END) {
                    startTime = startTime.plusDays(1).withHour(Schedule.WORKING_HOURS_START);
                } else {
                    startTime = startTime.plusHours(Schedule.SLOT_DURATION);
                }
            }
        }

    }

    private void isPlayersPerTeamValid(Long id, int playersPerTeam) {
        List<Team> teams = teamRepository.getTeamByTournamentIdAndStatus(id, TeamStatus.ACTIVE);
        List<Team> invalidTeams = new ArrayList<>();
        for (Team team : teams) {
            List<Account> accounts = accountRepository.findByTeamIdAndIsLocked(team.getId(), false);
            if (accounts.size() < playersPerTeam) {
                invalidTeams.add(team);
            }
        }

        if (!invalidTeams.isEmpty()) {

            StringBuilder msg = new StringBuilder();
            for (Team team : invalidTeams) {
                msg.append(team.getTeamName()).append(", ");
            }

            throw new DasrsException(HttpStatus.BAD_REQUEST, "The number of players in these teams is not enough: " + msg + ". Please check the team list");
        }

    }

    private void generateMatchTeam(Match match, List<Team> currentTeams, int playersPerTeam) {

        for (Team team : currentTeams) {
            for (int i = 0; i < playersPerTeam; i++) {
                MatchTeam matchTeam = new MatchTeam();
                matchTeam.setId(new MatchTeamId(match.getId(), team.getId()));
                matchTeam.setMatch(match);
                matchTeam.setTeam(team);
                matchTeam.setTeamTag(team.getTeamTag());
                matchTeam.setAttempt(0);
                matchTeamRepository.save(matchTeam);
            }
        }
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
        roundResponse.setFinishType(round.getMatchType().getFinishType());
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
    public List<RoundResponse> findAllRounds(int pageNo, int pageSize, RoundSort sortBy, String keyword) {

        Sort sort = Sort.by(sortBy.getField()).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Specification<Round> spec = Specification.where(RoundSpecification.hasRoundName(keyword));

        Page<Round> roundPage = roundRepository.findAll(spec, pageable);

        List<RoundResponse> roundResponses = new ArrayList<>();
        roundPage.getContent().forEach(round -> {
            RoundResponse roundResponse = findRoundByRoundId(round.getId());
            roundResponses.add(roundResponse);
        });

        return roundResponses;
    }

    //second, minute, hour, day, month, year
    //* = every
//    @Scheduled(cron = "5 * * * * *")
    @Async
    @Scheduled(cron = "1 0 0 * * *")
    @Transactional
    public void checkIfRoundEnd() {
        logger.info("Round end task is running");

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

            List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournament.getId());

            for (Round round : rounds) {
                if ((round.getStatus() == RoundStatus.PENDING || round.getStatus() == RoundStatus.ACTIVE) && round.getId() != roundEnd.get().getId()) {
                    List<Match> matches = matchRepository.findByRoundId(round.getId());
                    if (matches.isEmpty()) {
                        generateMatch(round, tournament);
                    }
                    return;
                }
            }

        }


        logger.info("Round end task is completed");
    }

    @Async
    @Scheduled(cron = "1 0 0 * * *")
    @Transactional
    public void checkIfRoundStart() {
        logger.info("Round start task is running");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();

        //Kiểm tra xem có vòng sẽ bắt đầu ko
        Optional<Round> round = roundRepository.findByStatusAndStartDateBefore(RoundStatus.PENDING, date);
        if (round.isPresent()) {
            logger.info("Found a round that reach start date");
            Tournament tournament = round.get().getTournament();
            if (tournament.getStatus() == TournamentStatus.ACTIVE) {
                round.get().setStatus(RoundStatus.ACTIVE);
                roundRepository.save(round.get());
                logger.info("Change round status to active. Round id: {}", round.get().getId());
            }
        }

        logger.info("Round start task is completed");
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
                        round.getMatchType() != null ? round.getMatchType().getFinishType() : null
                ))
                .collect(Collectors.toList());

        return GetRoundsByAccountResponse.builder()
                .rounds(roundResponses)
                .totalPages(roundsPage.getTotalPages())
                .totalElements(roundsPage.getTotalElements())
                .pageNo(roundsPage.getNumber())
                .pageSize(roundsPage.getSize())
                .last(roundsPage.isLast())
                .build();
    }

}
