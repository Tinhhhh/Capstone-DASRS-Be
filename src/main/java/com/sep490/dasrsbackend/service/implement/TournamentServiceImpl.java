package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.GenerateCode;
import com.sep490.dasrsbackend.Util.Schedule;
import com.sep490.dasrsbackend.Util.TournamentSpecification;
import com.sep490.dasrsbackend.converter.TeamConverter;
import com.sep490.dasrsbackend.dto.ParticipantDTO;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import com.sep490.dasrsbackend.model.enums.TournamentSort;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.EditTournament;
import com.sep490.dasrsbackend.model.payload.request.NewTournament;
import com.sep490.dasrsbackend.model.payload.response.*;
import com.sep490.dasrsbackend.repository.MatchRepository;
import com.sep490.dasrsbackend.repository.RoundRepository;
import com.sep490.dasrsbackend.repository.TeamRepository;
import com.sep490.dasrsbackend.repository.TournamentRepository;
import com.sep490.dasrsbackend.service.TournamentService;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final RoundRepository roundRepository;
    private final MatchRepository matchRepository;
    private final ModelMapper modelMapper;
    private final TeamRepository teamRepository;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TournamentServiceImpl.class);
    private final TeamConverter teamConverter;


    @Override
    public void createTournament(NewTournament newTournament) {

        if (!tournamentRepository.findByDate(newTournament.getStartDate())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "There is another tournament on this date.");
        }

        if (newTournament.getTeamNumber() <= 1) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Team number must be at least 2");
        }

        // Kiểm tra begin phải trước end
        newTournament.setStartDate(DateUtil.convertUTCtoICT(newTournament.getStartDate()));
        newTournament.setEndDate(DateUtil.convertUTCtoICT(newTournament.getEndDate()));

        LocalDateTime startDate = DateUtil.convertToLocalDateTime(newTournament.getStartDate()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endDate = DateUtil.convertToLocalDateTime(newTournament.getEndDate()).withHour(23).withMinute(59).withSecond(59);

        Date begin = DateUtil.convertToDate(startDate);
        Date end = DateUtil.convertToDate(endDate);

        if (begin.after(end)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Start date must be before end date.");
        }

//        tournamentValidation(begin, end, newTournament.getTeamNumber());

        String tournamentName = "[" + GenerateCode.seasonPrefix(startDate) + "] " + newTournament.getTournamentName().trim();

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

    private static void tournamentValidation(Date begin, Date end, int teamNumber) {
        Calendar calendar = Calendar.getInstance();

        if (begin.before(calendar.getTime())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Start date must be after today.");
        }

        // Kiểm tra begin phải cách hiện tại ít nhất 1 tuần và không quá 3 tháng
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        Date minBegin = calendar.getTime();
        calendar.add(Calendar.WEEK_OF_YEAR, 3);
        Date maxBegin = calendar.getTime();

//        if (begin.before(minBegin) || begin.after(maxBegin)) {
//            throw new DasrsException(HttpStatus.BAD_REQUEST,
//                    "Start date must be at least 1 weeks and no more than 4 weeks from today.");
//        }

        if (begin.after(maxBegin)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "Start date no more than 4 weeks from today.");
        }

        double atLeastDay = Math.ceil((double) teamNumber / Schedule.MAX_WORKING_HOURS);

        calendar.setTime(begin);
        calendar.add(Calendar.DAY_OF_WEEK, (int) atLeastDay);
        Date minEnd = calendar.getTime();

        calendar.setTime(begin);
        calendar.add(Calendar.WEEK_OF_YEAR, 4);
        Date maxEnd = calendar.getTime();

        if (end.before(minEnd) || end.after(maxEnd)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "The tournament duration is at least " + atLeastDay + " days and no more than 4 weeks after the start date.");
        }
    }

    @Override
    public void editTournament(Long tournamentId, EditTournament editTournament) {

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, tournament not found."));

        if (tournament.getStatus() == TournamentStatus.ACTIVE || tournament.getStatus() == TournamentStatus.COMPLETED || tournament.getStatus() == TournamentStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Cannot edit tournament while it is not pending.");
        }

        if (tournament.getStartDate().after(editTournament.getEndDate())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "End date must be after start date.");
        }

        isRoundValid(tournament);
        LocalDateTime startDate = DateUtil.convertToLocalDateTime(tournament.getStartDate());

        String tournamentName = "[" + GenerateCode.seasonPrefix(startDate) + "] " + editTournament.getTournamentName().trim();

        tournament.setTournamentName(tournamentName);
        tournament.setContext(editTournament.getTournamentContext());
        tournament.setTeamNumber(editTournament.getTeamNumber());
        tournament.setStartDate(editTournament.getStartDate());
        tournament.setEndDate(editTournament.getEndDate());

        tournamentRepository.save(tournament);
    }

    private void isRoundValid(Tournament tournament) {
        List<Round> roundList = roundRepository.findByTournamentIdAndStatus(tournament.getId(), RoundStatus.PENDING);
        if (!roundList.isEmpty()) {
            Date start = roundList.get(0).getStartDate();
            Date end = roundList.get(roundList.size() - 1).getEndDate();

            if (tournament.getStartDate().after(start)) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, cannot change tournament start date to after first round start date.");
            }
            if (tournament.getEndDate().before(end)) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, cannot change tournament end date to before last round end date.");
            }
        }
    }

    @Override
    public ListTournament getAllTournaments(int pageNo, int pageSize, TournamentSort sortBy, String keyword) {

        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());

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

        if (tournamentRepository.findByStatus(TournamentStatus.ACTIVE).isPresent()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "There is another tournament is active.");
        }

        //Kiểm tra có đáp ứng số ngày ko
        tournamentValidation(tournament.getStartDate(), tournament.getEndDate(), tournament.getTeamNumber());

        List<Round> roundList = roundRepository.findByTournamentIdAndStatus(id, RoundStatus.PENDING);

        if (roundList.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Start fails, cannot start tournament without rounds.");
        }

        List<Team> teamList = teamRepository.getTeamByTournamentId(id);
        if (teamList.size() != tournament.getTeamNumber()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Start fails, cannot start tournament without enough teams.");
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
                tournament.setStatus(TournamentStatus.PENDING);
            }

            if (status == TournamentStatus.TERMINATED) {
                //Kiểm tra xem có match nào đã khởi động không
                changeToTerminated(id);
                tournament.setStatus(TournamentStatus.TERMINATED);

            }
            tournamentRepository.save(tournament);
        }

    }

    @Override
    public void editTournamentSchedule(Long id, int day) {

        Tournament tournament = tournamentRepository.findByIdAndStatus(id, TournamentStatus.ACTIVE)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Tournament not found."));

        if (day > 7 || day < -7) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Tournament endDate cannot change > 7 days at once.");
        }

        List<Round> roundList = roundRepository.findByTournamentId(id);

        for (Round round : roundList) {
            if (round.getStatus() == RoundStatus.COMPLETED || round.getStatus() == RoundStatus.TERMINATED) {
                roundList.remove(round);
            }
        }

        //Để giảm thời gian end date của tournament có 2 trường hợp
        //1. khi tournament có last round
        //2. khi tournament không có last round

        Date endDate = roundList.get(roundList.size() - 1).getEndDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(tournament.getEndDate());
        calendar.add(Calendar.DAY_OF_WEEK, day);
        tournament.setEndDate(calendar.getTime());

        if (tournament.getEndDate().before(endDate)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Edit fails, cannot change tournament end date to before last round end date.");
        }

        tournamentRepository.save(tournament);
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

    @Override
    public List<ParticipantDTO> getUsersByTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Tournament not found"));

        List<ParticipantDTO> participants = new ArrayList<>();
        tournament.getTeamList().forEach(team -> {
            team.getAccountList().forEach(account -> {
                ParticipantDTO dto = new ParticipantDTO();
                dto.setAccountId(account.getAccountId());
                dto.setFirstName(account.getFirstName());
                dto.setLastName(account.getLastName());
                dto.setEmail(account.getEmail());
                dto.setAvatar(account.getAvatar());
                dto.setPhone(account.getPhone());
                dto.setGender(account.getGender());
                dto.setDob(account.getDob());
                participants.add(dto);
            });
        });

        return participants;
    }

    @Scheduled(cron = "1 0 0 * * *")
    public void endTournament() {
        logger.info("End tournament task is running.");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();

        //Kiểm tra xem có tournament nào kết thúc ko
        Optional<Tournament> tournament = tournamentRepository.findByStatusAndEndDateBefore(TournamentStatus.ACTIVE, date);
        if (tournament.isPresent()) {
            logger.info("Found a tournament that has reached the end date.");
            List<Round> rounds = roundRepository.findByTournamentIdAndStatus(tournament.get().getId(), RoundStatus.COMPLETED);

            boolean flag = false;
            for (Round round : rounds) {
                if (round.isLast()) {
                    flag = true;
                    break;
                }
            }

            if (flag) {
                tournament.get().setStatus(TournamentStatus.COMPLETED);
                tournamentRepository.save(tournament.get());
                logger.info("Tournament completed successfully. Tournament Id: {}", tournament.get().getId());
            } else {
                logger.error("There is no last round completed in tournament but tournament end date is reached. Tournament Id: {}", tournament.get().getId());
            }
        }

        logger.info("End tournament task is completed.");
    }

    public List<TeamResponse> getTeamsByTournamentId(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Tournament not found"));

        return tournament.getTeamList().stream()
                .map(teamConverter::convertToTeamResponse)
                .collect(Collectors.toList());
    }

}
