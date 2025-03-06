package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.Schedule;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.TournamentRuleException;
import com.sep490.dasrsbackend.model.payload.request.EditRound;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.model.payload.response.ListRound;
import com.sep490.dasrsbackend.model.payload.response.RoundResponse;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.RoundService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public void newRound(NewRound newRound) {

        Tournament tournament = tournamentRepository.findById(newRound.getTournamentId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Tournament not found"));

        MatchType matchType = matchTypeRepository.findById(newRound.getMatchTypeId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match Type not found"));

        ScoredMethod scoredMethod = scoredMethodRepository.findById(newRound.getScoredMethodId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Scored Method not found"));

        Environment environment = environmentRepository.findById(newRound.getEnvironmentId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Environment not found"));

        roundValidation(newRound, tournament, true);

        Round round = Round.builder()
                .roundName(newRound.getRoundName())
                .teamLimit(newRound.getTeamLimit())
                .description(newRound.getDescription())
                .status(RoundStatus.ACTIVE)
                .startDate(newRound.getStartDate())
                .endDate(newRound.getEndDate())
                .tournament(tournament)
                .matchType(matchType)
                .scoredMethod(scoredMethod)
                .environment(environment)
                .build();
        roundRepository.save(round);

        generateMatch(round, tournament);
    }

    private void roundValidation(NewRound newRound, Tournament tournament, boolean isNew) {
        List<Round> rounds = roundRepository.findByTournamentIdAndStatus(tournament.getId(), RoundStatus.ACTIVE);

        for (Round round : rounds) {
            if (round.isLast()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The last round has been created, can't create more round");
            }
        }

        if (rounds.size() == 3) {
            if (newRound.getTeamLimit() != 0) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be 0 for the last round");
            }
        }

        if (newRound.isLast() && newRound.getTeamLimit() != 0) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be 0 for the last round");
        }

        if (newRound.getTeamLimit() > tournament.getTeamNumber()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be less than or equal to the tournament team number: " + tournament.getTeamNumber());
        }

        LocalDateTime roundStartTime = DateUtil.convertToLocalDateTime(newRound.getStartDate());

        if (roundStartTime.isBefore(DateUtil.convertToLocalDateTime(tournament.getStartDate()))) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round start date is invalid, round start date must be after tournament start date: " + tournament.getStartDate());
        }

        int totalMatches = tournament.getTeamNumber();
        double totalHours = totalMatches * Schedule.SLOT_DURATION;

        if (Math.ceil(totalHours / Schedule.MAX_WORKING_HOURS) > Schedule.MAX_WORKING_DAYS) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round duration is too long, round duration is no more than 5 days");
        }

        if (isNew) {
            //validate số lượng vòng đã tạo
            if (!isAbleToCreateRound(tournament, rounds)) {
                throw new DasrsException(HttpStatus.BAD_REQUEST,
                        "Round creation is not allowed, the tournament has reached the maximum number of rounds. Tournament team is: " + tournament.getTeamNumber() + " and round is: " + rounds.size());
            }
        }

        //Trường hợp vòng đầu tiên
        LocalDateTime tStart = DateUtil.convertToLocalDateTime(tournament.getStartDate());
        LocalDateTime tEnd = DateUtil.convertToLocalDateTime(tournament.getEndDate());

        //Trường hợp đã có ít nhất 1 vòng
        if (!rounds.isEmpty()) {
            tStart = DateUtil.convertToLocalDateTime(rounds.get(rounds.size() - 1).getEndDate()).plusDays(1).withHour(Schedule.WORKING_HOURS_START).withMinute(0).withSecond(0).withNano(0);
            tEnd = DateUtil.convertToLocalDateTime(tournament.getEndDate());

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

        addNewRound(tStart, tEnd, tournament, rounds, newRound);
    }

    private boolean addNewRound(LocalDateTime tStart, LocalDateTime tEnd, Tournament tournament, List<Round> rounds, NewRound newRound) {

        //Maặc định khi round dđang đc tạo là round đầu tiên
        //Trong trường hợp tệ nhất thì sau mỗi round chỉ loại 1 đội =>
        int roundRemains = 9;
        if (rounds.size() == 1) {
            roundRemains = 6;
        }
        if (rounds.size() == 2) {
            roundRemains = 3;
        }

        if (rounds.size() == 3) {
            roundRemains = 0;
        }

        double currMatches = tournament.getTeamNumber();
        if (!rounds.isEmpty() && !newRound.isLast()) {
            currMatches = rounds.get(rounds.size() - 1).getTeamLimit();

            if (newRound.getTeamLimit() >= currMatches) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The round team limit is invalid, the team limit must be less than or the previous round team limit: " + currMatches);
            }
        }

        //Thời gian cần để tạo vòng này
        double currentMatchesNeeded = currMatches * Schedule.SLOT_DURATION;


        //Tg cần để tạo vòng tiếp theo
        double nextRoundHrsNeeded = newRound.getTeamLimit() * Schedule.SLOT_DURATION;
        //Tổng số ngày cần dể tạo ngày tiếp theo
        double nextRoundDayNeeded = Math.ceil(nextRoundHrsNeeded / Schedule.MAX_WORKING_HOURS) + roundRemains;

        if (newRound.isLast()) {
            nextRoundDayNeeded = 0;
        }

            LocalDateTime earliestStartTime = tStart.withHour(Schedule.WORKING_HOURS_START);
        LocalDateTime earliestEndTime = plusAtleastMatchHours(earliestStartTime, currentMatchesNeeded);

        //Thời gian kết thúc trễ nhất của vòng này sẽ bằng thời gian kết thúc của tournament trừ đi thời gian cần để tạo các vòng tiếp theo
        LocalDateTime latestEndTime = tEnd.minusDays((long) nextRoundDayNeeded).withHour(23).withMinute(59).withSecond(59);
        LocalDateTime latestStartTime = minusAtLeastMatchHours(latestEndTime.withHour(Schedule.WORKING_HOURS_END), currentMatchesNeeded);
        LocalDateTime localNewRoundStartTime = DateUtil.convertToLocalDateTime(newRound.getStartDate()).withHour(Schedule.WORKING_HOURS_START);
        LocalDateTime localNewRoundEndTime = DateUtil.convertToLocalDateTime(newRound.getEndDate()).withHour(23).withMinute(59).withSecond(59);

        if (localNewRoundStartTime.isBefore(earliestStartTime) || localNewRoundStartTime.isAfter(latestStartTime)) {
            Map<String, String> response = getDataResponse(earliestStartTime, latestEndTime, localNewRoundStartTime.isBefore(earliestStartTime), localNewRoundStartTime);

            throw new TournamentRuleException(HttpStatus.BAD_REQUEST,
                    "The round start date is invalid, round start date is between " + earliestStartTime + " and " + latestStartTime,
                    response);
        }

        if (localNewRoundEndTime.isBefore(earliestEndTime) || localNewRoundEndTime.isAfter(latestEndTime)) {
            Map<String, String> response = getDataResponse(earliestEndTime, latestEndTime, localNewRoundStartTime.isBefore(earliestEndTime), localNewRoundEndTime);

            throw new TournamentRuleException(HttpStatus.BAD_REQUEST,
                    "The round end date is invalid, round end date is between " + earliestEndTime + " and " + latestEndTime,
                    response);
        }

        if (!validateWorkingHours(localNewRoundStartTime, localNewRoundEndTime, (int) currentMatchesNeeded)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "The round schedule is invalid, the round must have at least " + currentMatchesNeeded +
                            " working hours, at least " + Math.ceil(currentMatchesNeeded / Schedule.MAX_WORKING_HOURS) + " days  to create matches");
        }

        return true;
    }

    private Map<String, String> getDataResponse(LocalDateTime earliestStartTime, LocalDateTime latestEndTime, boolean before, LocalDateTime localNewRoundStartTime) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("startDate", DateUtil.formatTimestamp(DateUtil.convertToDate(earliestStartTime), DateUtil.DATE_FORMAT));
        response.put("endDate", DateUtil.formatTimestamp(DateUtil.convertToDate(latestEndTime), DateUtil.DATE_FORMAT));
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

    private boolean isAbleToCreateRound(Tournament tournament, List<Round> rounds) {

        if (!rounds.isEmpty()) {
            if (rounds.size() >= Schedule.ROUND_LIMIT) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, Double> getRoundPrereq(Tournament tournament, List<Round> rounds, NewRound newRound) {
        //Đây là hàm kiểm tra xem thời gian tối thiểu cần thiết để tạo vòng sau cùng
        //Tuỳ theo số lượng đội tham gia và số vòng đã tạo ra mà sẽ có số trận đấu tối thiểu khác nhau
        Map<String, Double> roundPrereq = new HashMap<>();
        double currMatches = -1;
        int nextRoundMatches = newRound.getTeamLimit();
        int roundRemains = -1;
        if (tournament.getTeamNumber() < Schedule.TEAM_THRESHOLD) {
            //Trường hợp số lượng team tham gia < 15
            //Trường sẽ tạo round 1. số round cần thiết của round 2 là 5-7
            if (rounds.isEmpty()) {
                currMatches = tournament.getTeamNumber();
                roundRemains = 2;
            }

            //Trường hợp tạo round 2
            if (rounds.size() == 1) {
                //Có 5-7 đội tham gia
                currMatches = 7;
                roundRemains = 1;
            }

            if (rounds.size() == 2) {
                currMatches = 3;
                roundRemains = 0;
            }
        } else {
            //Trường hợp số lượng team tham gia >= 15
            if (rounds.isEmpty()) {
                currMatches = tournament.getTeamNumber();
                roundRemains = 3;
            }

            if (rounds.size() == 1) {
                currMatches = 10;
                roundRemains = 2;
            }

            if (rounds.size() == 2) {
                currMatches = 5;
                roundRemains = 1;
            }

            if (rounds.size() == 3) {
                currMatches = 3;
                roundRemains = 0;
            }

        }

        if (nextRoundMatches == -1 || roundRemains == -1 || currMatches == -1) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. Please check the total matches");
        }

        double currentRoundMatches = currMatches * Schedule.SLOT_DURATION;
        double totalHours = nextRoundMatches * Schedule.SLOT_DURATION;
        double totalDayNeeds = Math.ceil(totalHours / Schedule.MAX_WORKING_HOURS) + roundRemains;
        roundPrereq.put("currRoundMatches", currentRoundMatches);
        roundPrereq.put("nextRoundTimeNeeds", totalHours);
        roundPrereq.put("totalDayNeeds", totalDayNeeds);
        return roundPrereq;
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

    @Override
    public RoundResponse findRoundByRoundId(Long id) {
        return null;
    }

    @Override
    public ListRound findRoundByTournamentId(Long id) {
        return null;
    }

    @Override
    public void editRound(EditRound request) {
        Round round = roundRepository.findById(request.getId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

        Tournament tournament = tournamentRepository.findById(round.getTournament().getId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Tournament not found"));

        MatchType matchType = matchTypeRepository.findById(request.getMatchTypeId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match Type not found"));

        ScoredMethod scoredMethod = scoredMethodRepository.findById(request.getScoredMethodId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Scored Method not found"));

        Environment environment = environmentRepository.findById(request.getEnvironmentId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Environment not found"));

        NewRound newRound = modelMapper.map(request, NewRound.class);

        roundValidation(newRound, tournament, false);

        round = Round.builder()
                .roundName(newRound.getRoundName())
                .description(newRound.getDescription())
                .status(request.getStatus())
                .startDate(newRound.getStartDate())
                .endDate(newRound.getEndDate())
                .tournament(tournament)
                .matchType(matchType)
                .scoredMethod(scoredMethod)
                .environment(environment)
                .build();

        roundRepository.save(round);

    }

    private void generateMatch(Round round, Tournament tournament) {

        MatchType matchType = round.getMatchType();

        double matchDuration = matchType.getMatchDuration();
        Map<Integer, Team> randomTeam = new HashMap<>();
        int teamNumber = tournament.getTeamNumber();

        List<Round> rounds = roundRepository.findByTournamentIdAndStatus(tournament.getId(), RoundStatus.COMPLETED);

        if (rounds.size() > 1) {
            teamNumber = rounds.get(rounds.size() - 1).getTeamLimit();
        }

        for (Team team : tournament.getTeamList()) {
            randomTeam.put(teamNumber--, team);
        }

        LocalDateTime startTime = DateUtil.convertToLocalDateTime(round.getStartDate()).withHour(Schedule.WORKING_HOURS_START);
        LocalDateTime endTime = DateUtil.convertToLocalDateTime(round.getEndDate()).withHour(Schedule.WORKING_HOURS_END);

        LocalTime workStart = LocalTime.of(Schedule.WORKING_HOURS_START, 0);
        LocalTime workEnd = LocalTime.of(Schedule.WORKING_HOURS_END, 0);

        StringBuilder stringBuilder = new StringBuilder();
        String seasonPrefix = generateMatchCode(DateUtil.convertToLocalDateTime(tournament.getStartDate()));
        String matchTypePrefix = matchType.getMatchTypeCode();

        while (startTime.isBefore(endTime) && !randomTeam.isEmpty()) {
            LocalTime currentTime = startTime.toLocalTime();
            // Nếu trong khung giờ làm việc (8:00 - 17:00), tăng biến đếm
            if (!currentTime.isBefore(workStart) && currentTime.isBefore(workEnd) && currentTime.getHour() != Schedule.LUNCH_BREAK_START) {

                //Bóc tách match type code để lấy số đội mỗi trận đấu và số ng tham gia mỗi đội
                String[] parts = matchTypePrefix.split("-");
                int teamCount = 0;
                int playersPerTeam = 0;
                if (parts.length > 1) {
                    String numberPart = parts[1].substring(0, 2);
                    teamCount = Character.getNumericValue(numberPart.charAt(0)); // Số đội
                    playersPerTeam = Character.getNumericValue(numberPart.charAt(1)); // Số người mỗi đội

                    if (teamCount == 0 || playersPerTeam == 0) {
                        throw new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "Match type code is invalid, please contact the administrator");
                    }

                } else {
                    throw new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "Match type code is invalid, please contact the administrator");
                }

                //Tạo match name
                String name = stringBuilder
                        .append(matchType.getMatchTypeName())
                        .append(" - ")
                        .toString();

                //Chọn đội tham gia trận đấu ngẫu nhiên
                for (int i = 0; i < teamCount; i++) {
                    Team team = randomTeam.get(teamNumber);
                    randomTeam.remove(teamNumber++);
                    if (i >= 1) {
                        name = stringBuilder.append(", ").toString();
                    }
                    name = stringBuilder.append(team.getTeamName()).toString();
                }

                //Tạo code random cho trận đấu
                SecureRandom random = new SecureRandom();
                StringBuilder number = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    int digit = random.nextInt(10); // Sinh số từ 0-9
                    number.append(digit);
                }

                //Ghép lại thành match code
                String matchCode = stringBuilder
                        .append(seasonPrefix)
                        .append("_")
                        .append(matchTypePrefix)
                        .append("_")
                        .append(number)
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

    private String generateMatchCode(LocalDateTime time) {
        StringBuilder StringBuilder = new StringBuilder();
        int year = time.getYear();
        Month month = time.getMonth();

        String season;
        if (month.getValue() >= 3 && month.getValue() <= 5) {
            season = "SP"; //Spring
        } else if (month.getValue() >= 6 && month.getValue() <= 8) {
            season = "SM"; //Summer
        } else if (month.getValue() >= 9 && month.getValue() <= 11) {
            season = "FA"; //Fall
        } else {
            season = "WT"; //Winter
        }

        return StringBuilder.append(year).append(season).toString();

    }

}
