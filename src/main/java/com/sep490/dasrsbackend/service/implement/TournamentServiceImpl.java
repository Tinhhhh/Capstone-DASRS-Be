package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.ScoredMethod;
import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.NewTournament;
import com.sep490.dasrsbackend.model.payload.response.ListScoredMethod;
import com.sep490.dasrsbackend.model.payload.response.ScoredMethodResponse;
import com.sep490.dasrsbackend.repository.TournamentRepository;
import com.sep490.dasrsbackend.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final ModelMapper modelMapper;


    @Override
    public void createTournament(NewTournament newTournament) {

        LocalDateTime startDate = DateUtil.convertToLocalDateTime(newTournament.getStartDate()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endDate = DateUtil.convertToLocalDateTime(newTournament.getEndDate()).withHour(23).withMinute(59).withSecond(59);

        Date begin = DateUtil.convertToDate(startDate);
        Date end = DateUtil.convertToDate(endDate);

        TournamentValidation(begin, end);

        Tournament tournament = Tournament.builder()
                .tournamentName(newTournament.getTournamentName())
                .context(newTournament.getTournamentContext())
                .teamNumber(newTournament.getTeamNumber())
                .startDate(begin)
                .endDate(end)
                .status(TournamentStatus.PENDING)
                .build();

        tournamentRepository.save(tournament);
    }

    private static void TournamentValidation(Date begin, Date end) {
        Calendar calendar = Calendar.getInstance();

        // Kiểm tra begin phải cách hiện tại ít nhất 2 tuần và không quá 1 tháng
        calendar.add(Calendar.WEEK_OF_YEAR, 2);
        Date minBegin = calendar.getTime();
        calendar.add(Calendar.WEEK_OF_YEAR, 2);
        Date maxBegin = calendar.getTime();

        if (begin.before(minBegin) || begin.after(maxBegin)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "Start date must be at least 2 weeks and no more than 1 month from today.");
        }

        // Kiểm tra begin phải trước end
        if (begin.after(end)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Start date must be before end date.");
        }

        calendar.setTime(begin);
        calendar.add(Calendar.WEEK_OF_YEAR, 2);
        Date minEnd = calendar.getTime();

        calendar.setTime(begin);
        calendar.add(Calendar.MONTH, 1);
        Date maxEnd = calendar.getTime();

        if (end.before(minEnd) || end.after(maxEnd)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "End date must be at least 2 weeks and no more than 1 months after the start date.");
        }
    }

    @Override
    public void editTournament(NewTournament newTournament) {

    }

    @Override
    public Object getAllTournaments(int pageNo, int pageSize, String sortBy, String sortDirection) {
        return null;
    }

    @Override
    public Object getTournament(Long id) {
        return null;
    }

//    @Scheduled(cron = "0 0 2 * * *")
//    public void scheduledDeleteExpiredResetPasswordToken() {
//        resetPasswordTokenRepository.deleteTokensByRevokedTrue();
//    }
}
