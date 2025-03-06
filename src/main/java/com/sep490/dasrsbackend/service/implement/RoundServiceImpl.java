package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.Schedule;
import com.sep490.dasrsbackend.model.entity.*;
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

import java.time.LocalDateTime;
import java.time.LocalTime;
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

    }

    private void roundValidation(NewRound newRound, Tournament tournament, boolean isNew) {
        List<Round> rounds = roundRepository.findByTournamentIdAndStatus(tournament.getId(), RoundStatus.ACTIVE);

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
            tStart = DateUtil.convertToLocalDateTime(rounds.get(rounds.size() - 1).getEndDate()).plusDays(1).withHour(Schedule.WORKING_HOURS_START);
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


        Map<String, Double> roundPrereq = getRoundPrereq(tournament, rounds);
        double totalDaysNeeded = roundPrereq.get("totalDayNeeds");
        double currRoundMatches = roundPrereq.get("currRoundMatches");

        LocalDateTime earliestStartTime = tStart.withHour(Schedule.WORKING_HOURS_START);
        LocalDateTime earliestEndTime = plusAtleastMatchHours(earliestStartTime, currRoundMatches);

        //Lấy ngày cuối tuần đổi thành ngày cuôi cùng có thể bắt dđầu vòng đấu trừ đi số giờ trận đấu
        LocalDateTime latestEndTime = tEnd.minusDays((long) totalDaysNeeded).withHour(23).withMinute(59).withSecond(59);
        LocalDateTime latestStartTime = minusAtLeastMatchHours(latestEndTime, currRoundMatches);
        LocalDateTime localNewRoundStartTime = DateUtil.convertToLocalDateTime(newRound.getStartDate()).withHour(Schedule.WORKING_HOURS_START);
        LocalDateTime localNewRoundEndTime = DateUtil.convertToLocalDateTime(newRound.getEndDate()).withHour(23).withMinute(59).withSecond(59);

        if (localNewRoundStartTime.isBefore(earliestStartTime) || localNewRoundStartTime.isAfter(latestStartTime)) {
            Map<String, String> response = getDataResponse(earliestStartTime, latestEndTime, localNewRoundStartTime.isBefore(earliestStartTime), localNewRoundStartTime);

            throw new TournamentRuleException(HttpStatus.BAD_REQUEST,
                    "The round end date is invalid, round start date is between " + earliestStartTime + " and " + latestStartTime,
                    response);
        }

        if (localNewRoundEndTime.isBefore(earliestEndTime) || localNewRoundEndTime.isAfter(latestEndTime)) {
            Map<String, String> response = getDataResponse(earliestEndTime, latestEndTime, localNewRoundStartTime.isBefore(earliestEndTime), localNewRoundEndTime);

            throw new TournamentRuleException(HttpStatus.BAD_REQUEST,
                    "The round end date is invalid, round end date is between " + earliestEndTime + " and " + latestEndTime,
                    response);
        }

        if (!validateWorkingHours(localNewRoundStartTime, localNewRoundEndTime, (int) currRoundMatches)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "The round schedule is invalid, the round must have at least " + currRoundMatches +
                            " working hours, at least " + Math.ceil(currRoundMatches / Schedule.MAX_WORKING_HOURS) + " days  to create matches");
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
            //Neu so luong doi tham gia < 15 va so luong vong da tao >= 3
            if (tournament.getTeamNumber() < 15 && rounds.size() >= 3) {
                return false;
            }

            //Neu so luong doi tham gia >= 15 va so luong vong da tao >= 4
            if (tournament.getTeamNumber() >= 15 && rounds.size() >= 4) {
                return false;
            }

        }
        return true;
    }

    private static Map<String, Double> getRoundPrereq(Tournament tournament, List<Round> rounds) {
        //Đây là hàm kiểm tra xem thời gian tối thiểu cần thiết để tạo vòng sau cùng
        //Tuỳ theo số lượng đội tham gia và số vòng đã tạo ra mà sẽ có số trận đấu tối thiểu khác nhau
        Map<String, Double> roundPrereq = new HashMap<>();
        double currMatches = -1;
        int nextRoundMatches = -1;
        int roundRemains = -1;
        if (tournament.getTeamNumber() < Schedule.TEAM_THRESHOLD) {
            //Trường hợp số lượng team tham gia < 15
            //Trường sẽ tạo round 1. số round cần thiết của round 2 là 5-7
            if (rounds.isEmpty()) {
                currMatches = tournament.getTeamNumber();
                nextRoundMatches = 7;
                roundRemains = 2;
            }

            //Trường hợp tạo round 2
            if (rounds.size() == 1) {
                //Có 5-7 đội tham gia
                currMatches = 7;
                nextRoundMatches = 3;
                roundRemains = 1;
            }

            if (rounds.size() == 2) {
                currMatches = 3;
                nextRoundMatches = 0;
                roundRemains = 0;
            }
        } else {
            //Trường hợp số lượng team tham gia >= 15
            if (rounds.isEmpty()) {
                currMatches = tournament.getTeamNumber();
                nextRoundMatches = 10;
                roundRemains = 3;
            }

            if (rounds.size() == 1) {
                currMatches = 10;
                nextRoundMatches = 5;
                roundRemains = 2;
            }

            if (rounds.size() == 2) {
                currMatches = 5;
                nextRoundMatches = 3;
                roundRemains = 1;
            }

            if (rounds.size() == 3) {
                currMatches = 3;
                nextRoundMatches = 0;
                roundRemains = 0;
            }

        }

        if (nextRoundMatches == -1 || roundRemains == -1 || currMatches == -1) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. Please check the total matches");
        }

        double currentRoundMatches = currMatches * Schedule.SLOT_DURATION;
        double totalHours = nextRoundMatches * Schedule.SLOT_DURATION;
        double totalDayNeeds = Math.floor(totalHours / Schedule.MAX_WORKING_HOURS) + roundRemains;
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

}
