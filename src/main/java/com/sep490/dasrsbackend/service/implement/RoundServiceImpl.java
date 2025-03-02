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
                    "You can't create new round. Tournament team is: " + tournament.getTeamNumber() + " and round is: " + rounds.size());
        }

        //valida thời gian bắt đầu và kết thúc của vòng mới
        if (!isScheduleValid(tournament, rounds, matchType, newRound)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round schedule is invalid");
        }

        Round round = modelMapper.map(newRound, Round.class);
        round.setStatus(RoundStatus.ACTIVE);
        roundRepository.save(round);

    }

    private boolean isScheduleValid(Tournament tournament, List<Round> rounds, MatchType matchType, NewRound newRound) {

        LocalDateTime roundStartTime = DateUtil.convertToLocalDateTime(newRound.getStartDate());

        if (roundStartTime.getDayOfWeek() == DayOfWeek.SATURDAY || roundStartTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round start date is invalid, round start date is not on weekend");
        }

        //Ví dụ có 13 team và 1 match là 0.5h
        int totalMatches = tournament.getTeamNumber();
        double matchDuration = matchType.getMatchDuration();
        double totalHours = totalMatches * matchDuration;

        //Vậy 1 vòng sẽ cần 13x0.5 = 6.5h / 8 => Làm tròn cần ít nhất 1 ngày.
        double atLeastDays = Math.ceil(totalHours / Schedule.MAX_WORKING_HOURS);

        if (atLeastDays > Schedule.MAX_WORKING_DAYS) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round duration is too long, round duration is no more than 5 days");
        }

        if (!rounds.isEmpty()) {
            //Lấy thời gian kết thúc của vòng trước
            roundStartTime = DateUtil.convertToLocalDateTime(rounds.get(rounds.size() - 1).getEndDate());
            //Luôn bắt đầu từ 8h sáng
            roundStartTime = adjustToNextWorkingDayForStartTime(roundStartTime).withHour(Schedule.WORKING_HOURS_START);

        }

        LocalDateTime roundEndTime = roundStartTime;

        roundEndTime =  plusAtleastMatchHours(roundEndTime, totalHours);

        LocalDateTime minStartTime = roundStartTime;
        LocalDateTime maxStartTime = plusDayForMaxStartTime(roundStartTime, (long) (Schedule.MAX_WORKING_DAYS - atLeastDays));
        LocalDateTime minEndTime = roundEndTime;
        LocalDateTime maxEndTime = plusAtleastMatchHours(maxStartTime, totalHours);

        LocalDateTime localNewRoundStartTime = DateUtil.convertToLocalDateTime(newRound.getStartDate());
        LocalDateTime localNewRoundEndTime = DateUtil.convertToLocalDateTime(newRound.getEndDate());

        if (localNewRoundStartTime.isBefore(minStartTime) || localNewRoundStartTime.isAfter(maxStartTime)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round start date is invalid, round start date is between " + minStartTime + " and " + maxStartTime);
        }

        if (localNewRoundEndTime.isBefore(minEndTime) || localNewRoundEndTime.isAfter(maxEndTime)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round end date is invalid, round end date is between " + minEndTime + " and " + maxEndTime);
        }

        return true;
    }

    private LocalDateTime plusAtleastMatchHours(LocalDateTime roundEndTime, double totalHours) {
        for (int i = 0; i < totalHours; i++) {
            roundEndTime = roundEndTime.plusHours(Schedule.SLOT_DURATION);
            if (isLunchBreak(roundEndTime)) {
                roundEndTime = roundEndTime.withHour(Schedule.LUNCH_BREAK_END);
            }
            if (roundEndTime.getDayOfWeek() == DayOfWeek.FRIDAY && roundEndTime.getHour() > Schedule.WORKING_HOURS_END) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error. The round duration is too long, round duration is no more than 5 days");
            }
        }
    }


    private LocalDateTime adjustToNextWorkingDayForStartTime(LocalDateTime dateTime) {
        if (dateTime.getDayOfWeek() == DayOfWeek.FRIDAY && dateTime.getHour() >= 17) {
            dateTime = dateTime.plusDays(3);
        } else if (dateTime.getDayOfWeek() == DayOfWeek.SATURDAY) {
            dateTime = dateTime.plusDays(2);
        } else if (dateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dateTime = dateTime.plusDays(1);
        } else if (dateTime.getHour() >= 17) {
            dateTime = dateTime.plusDays(1);
        }
        return dateTime;
    }

    private LocalDateTime plusDayForMaxStartTime(LocalDateTime startTime, long days) {

        for (int i = 0; i < days; i++) {
            startTime = startTime.plusDays(1);
            if (startTime.getDayOfWeek() == DayOfWeek.FRIDAY) {
                return startTime;
            } else if (startTime.getDayOfWeek() == DayOfWeek.SATURDAY) {
                startTime = startTime.minusDays(1);
                return startTime;
            }
        }

        return startTime;
    }

    private LocalDateTime plusDayForMaxEndTime(LocalDateTime dateTime) {

        return dateTime;
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
