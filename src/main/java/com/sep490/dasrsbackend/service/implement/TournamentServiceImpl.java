package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.GenerateCode;
import com.sep490.dasrsbackend.Util.TournamentSpecification;
import com.sep490.dasrsbackend.model.entity.Match;
import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.entity.Tournament;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import com.sep490.dasrsbackend.model.enums.TournamentSort;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.NewTournament;
import com.sep490.dasrsbackend.model.payload.response.ListTournament;
import com.sep490.dasrsbackend.model.payload.response.RoundResponse;
import com.sep490.dasrsbackend.model.payload.response.TeamTournamentResponse;
import com.sep490.dasrsbackend.model.payload.response.TournamentResponse;
import com.sep490.dasrsbackend.repository.MatchRepository;
import com.sep490.dasrsbackend.repository.RoundRepository;
import com.sep490.dasrsbackend.repository.TeamRepository;
import com.sep490.dasrsbackend.repository.TournamentRepository;
import com.sep490.dasrsbackend.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final RoundRepository roundRepository;
    private final MatchRepository matchRepository;
    private final ModelMapper modelMapper;
    private final TeamRepository teamRepository;


    @Override
    public void createTournament(NewTournament newTournament) {

        LocalDateTime startDate = DateUtil.convertToLocalDateTime(newTournament.getStartDate()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endDate = DateUtil.convertToLocalDateTime(newTournament.getEndDate()).withHour(23).withMinute(59).withSecond(59);

        Date begin = DateUtil.convertToDate(startDate);
        Date end = DateUtil.convertToDate(endDate);

        tournamentValidation(begin, end);

        String tournamentName = "[" + GenerateCode.seasonPrefix(startDate) + "] " +newTournament.getTournamentName().trim();

        Tournament tournament = Tournament.builder()
                .tournamentName(tournamentName)
                .context(newTournament.getTournamentContext())
                .teamNumber(newTournament.getTeamNumber())
                .startDate(begin)
                .endDate(end)
                .status(TournamentStatus.PENDING)
                .build();

        tournamentRepository.save(tournament);
    }

    private static void tournamentValidation(Date begin, Date end) {
        Calendar calendar = Calendar.getInstance();

        // Kiểm tra begin phải cách hiện tại ít nhất 1 tuần và không quá 1 tháng
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        Date minBegin = calendar.getTime();
        calendar.add(Calendar.WEEK_OF_YEAR, 2);
        Date maxBegin = calendar.getTime();

        if (begin.before(minBegin) || begin.after(maxBegin)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "Start date must be at least 1 weeks and no more than 1 month from today.");
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
    public ListTournament getAllTournaments(int pageNo, int pageSize, TournamentSort sortBy, String keyword) {

        Sort sort =Sort.by(sortBy.getDirection(), sortBy.getField());

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Specification<Tournament> spec = Specification.where(TournamentSpecification.hasTournamentName(keyword));

        Page<Tournament> tournamentContents = tournamentRepository.findAll(spec, pageable);
        List<Tournament> tournaments = tournamentContents.getContent();

        List<TournamentResponse> content = new ArrayList<>();

        tournaments.forEach(tournamentResponse -> {
            TournamentResponse tournament = getTournament(tournamentResponse.getId());
            content.add(tournament);
        });

        return ListTournament.builder()
                .content(content)
                .totalPages(tournamentContents.getTotalPages())
                .totalElements(tournamentContents.getTotalElements())
                .pageNo(tournamentContents.getNumber())
                .pageSize(tournamentContents.getSize())
                .last(tournamentContents.isLast())
                .build();
    }

    @Override
    public TournamentResponse getTournament(Long id) {

        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Tournament not found."));

        List<Round> roundList = roundRepository.findByTournamentId(id);
        List<Team> teamList = teamRepository.getTeamByTournamentId(id);

        modelMapper.getConfiguration().setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setAmbiguityIgnored(true)
                .setSkipNullEnabled(false)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        List<RoundResponse> roundResponses = getRoundResponses(roundList);
        List<TeamTournamentResponse> teamResponses = getTeamResponses(teamList);

        return TournamentResponse.builder()
                .id(tournament.getId())
                .tournamentName(tournament.getTournamentName())
                .context(tournament.getContext())
                .teamNumber(tournament.getTeamNumber())
                .status(tournament.getStatus())
                .startDate(DateUtil.formatTimestamp(tournament.getStartDate()))
                .endDate(DateUtil.formatTimestamp(tournament.getEndDate()))
                .createdDate(DateUtil.formatTimestamp(tournament.getCreatedDate()))
                .roundList(roundResponses)
                .teamList(teamResponses)
                .build();

    }

    private List<TeamTournamentResponse> getTeamResponses(List<Team> teamList) {
        List<TeamTournamentResponse> teamResponses = new ArrayList<>();
        teamList.forEach(team -> {
            TeamTournamentResponse teamResponse = modelMapper.map(team, TeamTournamentResponse.class);
            teamResponses.add(teamResponse);
        });

        return teamResponses;
    }

    private List<RoundResponse> getRoundResponses(List<Round> roundList) {
        List<RoundResponse> roundResponses = new ArrayList<>();

        roundList.forEach(round -> {
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
            roundResponses.add(roundResponse);
        });

        return roundResponses;
    }

    @Override
    public void startTournament(Long id) {

        //kiểm tra xem tournament có tồn tại không
        Tournament tournament = tournamentRepository.findByIdAndStatus(id, TournamentStatus.PENDING)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Tournament not found."));

        //Kiểm tra có đáp ứng số ngày ko
        tournamentValidation(tournament.getStartDate(), tournament.getEndDate());
        List<Round> roundList = roundRepository.findByTournamentId(id);

        if (roundList.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Cannot start tournament without rounds.");
        }

        for (Round round : roundList) {
            round.setStatus(RoundStatus.ACTIVE);
            roundRepository.save(round);
        }

        tournament.setStatus(TournamentStatus.ACTIVE);
        tournamentRepository.save(tournament);

    }

    @Override
    public void changeStatus(Long id, TournamentStatus status) {

        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Tournament not found."));

        if (tournament.getStatus() == TournamentStatus.COMPLETED || tournament.getStatus() == TournamentStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Can't perform any actions, Tournament has been completed or terminated.");
        }

        if (status == TournamentStatus.COMPLETED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Tournament ca not changed to COMPLETED by this way");
        }

        if (status == TournamentStatus.ACTIVE) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Tournament can not start by this way, please use startTournament method");
        }

        //Nếu là active thì chỉ có thể thành pending, hoặc finish
        if (tournament.getStatus() == TournamentStatus.ACTIVE) {
            if (status == TournamentStatus.PENDING) {
                List<Round> roundList = roundRepository.findByTournamentIdAndStatus(id, RoundStatus.ACTIVE);

                for (Round round : roundList) {
                    List<Match> mathList = matchRepository.findByRoundId(round.getId()).stream()
                            .filter(match -> match.getTimeStart().before(new Date())).toList();
                    if (!mathList.isEmpty()) {
                        throw new DasrsException(HttpStatus.BAD_REQUEST, "Cannot change status to PENDING while there are active matches.");
                    }
                }

                //Đổi status của round thành Pending bởi vì tournament đổi thành chưa active
                roundList = roundRepository.findByTournamentId(id);
                for (Round round : roundList) {
                    round.setStatus(RoundStatus.PENDING);
                    roundRepository.save(round);
                }

                tournament.setStatus(TournamentStatus.PENDING);
            }

            if (status == TournamentStatus.TERMINATED) {
                //Kiểm tra xem có match nào đã khởi động không
                changeToTerminated(id);
                tournament.setStatus(TournamentStatus.TERMINATED);

            }

            //Nếu là pending thì chỉ có thể thành active, hoặc terminate


            tournamentRepository.save(tournament);
        }

        if (tournament.getStatus() == TournamentStatus.PENDING) {

            if (status == TournamentStatus.PENDING) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Tournament is already in PENDING status.");
            }

            if (status != TournamentStatus.TERMINATED) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal server error, please contact admin.");
            }

            //Kiểm tra xem có match nào đã khởi động không
            changeToTerminated(id);
            tournament.setStatus(TournamentStatus.TERMINATED);
            tournamentRepository.save(tournament);
        }

    }

    private void changeToTerminated(Long id) {
        List<Round> roundList = roundRepository.findByTournamentIdAndStatus(id, RoundStatus.ACTIVE);

        for (Round round : roundList) {
            List<Match> mathList = matchRepository.findByRoundId(round.getId()).stream()
                    .filter(match -> match.getTimeStart().before(new Date())).toList();
            if (!mathList.isEmpty()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Cannot change status to TERMINATED while there are active matches.");
            }
        }

        //Đổi status của round, match thành terminated, huỷ bỏ
        roundList = roundRepository.findByTournamentId(id);

        for (Round round : roundList) {
            round.setStatus(RoundStatus.TERMINATED);
            roundRepository.save(round);
        }

        for (Round round : roundList) {
            List<Match> matchList = matchRepository.findByRoundId(round.getId());
            for (Match match : matchList) {
                match.setStatus(MatchStatus.CANCELLED);
                matchRepository.save(match);
            }

        }
    }
}
