package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.Schedule;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.model.payload.response.ListRound;
import com.sep490.dasrsbackend.model.payload.response.RoundResponse;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.RoundService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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

        List<Round> rounds = roundRepository.findByTournamentIdAndStatus(tournament.getId(), RoundStatus.ACTIVE);

        //validate số lượng vòng đã tạo
        if (!isAbleToCreateRound(tournament, rounds)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "Round creation is not allowed, the tournament has reached the maximum number of rounds. Tournament team is: " + tournament.getTeamNumber() + " and round is: " + rounds.size());
        }

        //Trường hợp vòng đầu tiên
        if (rounds.isEmpty()) {
            if (!isNewRoundvalid(tournament, newRound)) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round schedule is invalid");
            }
        }

        //Trường hợp đã có ít nhất 1 vòng và số lượng team tham gia < 15
        if (!rounds.isEmpty() && tournament.getTeamNumber() < Schedule.TEAM_THRESHOLD) {
            if (rounds.size() < 3) {
                if (!addNewRound(tournament, rounds, newRound)) {
                    throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round schedule is invalid");
                }
            } else {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Round creation is not allowed, the tournament has reached the maximum number of rounds");
            }
        }

        //Trường hợp đã có ít nhất 1 vòng và số lượng team tham gia >= 15
        if (!rounds.isEmpty() && tournament.getTeamNumber() >= Schedule.TEAM_THRESHOLD) {
            if (rounds.size() < 4) {
                if (!addNewRound(tournament, rounds, newRound)) {
                    throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round schedule is invalid");
                }
            } else {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Round creation is not allowed, the tournament has reached the maximum number of rounds");
            }
        }

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

    private boolean addNewRound(Tournament tournament, List<Round> rounds, NewRound newRound) {

        double totalHours = getTotalHours(tournament, rounds);

        Date lastDate = rounds.get(rounds.size() - 1).getEndDate();
        //Giãn cách ít nhất 1 ngày từ ngày kết thúc vòng trước đến ngày bắt đầu vòng mới
        LocalDateTime roundStartTime = DateUtil.convertToLocalDateTime(lastDate).plusDays(1);

        LocalDateTime roundEndTime = roundStartTime;
        while (roundEndTime.getDayOfWeek() != DayOfWeek.SUNDAY) {
            roundEndTime = roundEndTime.plusDays(1);
        }

        LocalDateTime earliestStartTime = roundStartTime.withHour(Schedule.WORKING_HOURS_START);
        LocalDateTime earliestEndTime = plusAtleastMatchHoursForStartTime(earliestStartTime, totalHours);

        LocalDateTime latestEndTime = roundEndTime.withHour(Schedule.WORKING_HOURS_END + 1);
        LocalDateTime latestStartTime = minusAtLeastMatchHours(latestEndTime, totalHours);

        LocalDateTime localNewRoundStartTime = DateUtil.convertToLocalDateTime(newRound.getStartDate());
        LocalDateTime localNewRoundEndTime = DateUtil.convertToLocalDateTime(newRound.getEndDate());

        if (localNewRoundStartTime.isBefore(earliestStartTime) || localNewRoundStartTime.isAfter(latestStartTime)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round start date is invalid, round start date is between " + earliestStartTime + " and " + latestStartTime);
        }

        if (localNewRoundEndTime.isBefore(earliestEndTime) || localNewRoundEndTime.isAfter(latestEndTime)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round end date is invalid, round end date is between " + earliestEndTime + " and " + latestEndTime);
        }

        return true;
    }

    private static double getTotalHours(Tournament tournament, List<Round> rounds) {
        //Mặc định số trận đấu của vòng cuối cùng là 3
        int totalMatches = 3;
        if (tournament.getTeamNumber() < Schedule.TEAM_THRESHOLD) {
            //Nếu số lượng team tham gia < 15 và số lượng vòng đã tạo = 1 thì round 2 tổng số trận đấu là 5
            if (rounds.size() == 1) {
                totalMatches = 5;
            }

        } else {
            if (rounds.size() < 4) {
                //Nếu số lượng team tham gia >= 15 và số lượng vòng đã tạo = 1 thì round 2 tổng số trận đấu là 10
                if (rounds.size() == 1) {
                    totalMatches = 10;
                }
                //Nếu số lượng team tham gia >= 15 và số lượng vòng đã tạo = 2 thì round 3 tổng số trận đấu là 5
                if (rounds.size() == 2) {
                    totalMatches = 5;
                }
            }
        }

        double totalHours = totalMatches * Schedule.SLOT_DURATION;

        if (Math.ceil(totalHours / Schedule.MAX_WORKING_HOURS) > Schedule.MAX_WORKING_DAYS) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round duration is too long, round duration is no more than 5 days");
        }
        return totalHours;
    }

    private boolean isNewRoundvalid(Tournament tournament, NewRound newRound) {

        LocalDateTime tStartTime = DateUtil.convertToLocalDateTime(tournament.getStartDate());
        LocalDateTime roundStartTime = DateUtil.convertToLocalDateTime(newRound.getStartDate());

        if (roundStartTime.isBefore(tStartTime)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round start date is invalid, round start date must be after tournament start date");
        }

        //Ví dụ có 13 team, 1 match cố định là 1h
        int totalMatches = tournament.getTeamNumber();
        double totalHours = totalMatches * Schedule.SLOT_DURATION;

        if (Math.ceil(totalHours / Schedule.MAX_WORKING_HOURS) > Schedule.MAX_WORKING_DAYS) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round duration is too long, round duration is no more than 5 days");
        }

        //Lấy ngày bắt đầu vòng đầu tiên để tìm ngày cuối tuần
        LocalDateTime roundEndTime = roundStartTime;
        while (roundEndTime.getDayOfWeek() != DayOfWeek.SUNDAY) {
            roundEndTime = roundEndTime.plusDays(1);
        }

        LocalDateTime earliestStartTime = tStartTime.withHour(Schedule.WORKING_HOURS_START);

        LocalDateTime earliestEndTime = plusAtleastMatchHours(earliestStartTime, totalHours);

        //Lấy ngày cuối tuần đổi thành ngày cuôi cùng có thể baắt dđầu vòng đấu trừ đi số giờ trận đấu
        LocalDateTime latestEndTime = roundEndTime.withHour(Schedule.WORKING_HOURS_END + 1);

        LocalDateTime latestStartTime = minusAtLeastMatchHours(latestEndTime, totalHours);

        LocalDateTime localNewRoundStartTime = DateUtil.convertToLocalDateTime(newRound.getStartDate());
        LocalDateTime localNewRoundEndTime = DateUtil.convertToLocalDateTime(newRound.getEndDate());

        if (localNewRoundStartTime.isBefore(earliestStartTime) || localNewRoundStartTime.isAfter(latestStartTime)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round start date is invalid, round start date is between " + earliestStartTime + " and " + latestStartTime);
        }

        if (localNewRoundEndTime.isBefore(earliestEndTime) || localNewRoundEndTime.isAfter(latestEndTime)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The round end date is invalid, round end date is between " + earliestEndTime + " and " + latestEndTime);
        }

        return true;

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

    private LocalDateTime plusAtleastMatchHoursForStartTime(LocalDateTime roundEndTime, double totalHours) {
        for (int i = 0; i < totalHours; i++) {
            roundEndTime = roundEndTime.plusHours(Schedule.SLOT_DURATION);
            if (isLunchBreak(roundEndTime)) {
                roundEndTime = roundEndTime.withHour(Schedule.LUNCH_BREAK_END);
            }
            if (roundEndTime.getHour() == Schedule.WORKING_HOURS_END) {
                roundEndTime = roundEndTime.plusDays(1).withHour(Schedule.WORKING_HOURS_START);
            }
        }
        return roundEndTime;
    }

    private boolean isLunchBreak(LocalDateTime dateTime) {
        return dateTime.getHour() >= Schedule.LUNCH_BREAK_START && dateTime.getHour() < Schedule.LUNCH_BREAK_END;
    }


    @Override
    public RoundResponse findRoundByRoundId(Long id) {
        return null;
    }

    @Override
    public ListRound findRoundByTournamentId(Long id) {
        return null;
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
}
