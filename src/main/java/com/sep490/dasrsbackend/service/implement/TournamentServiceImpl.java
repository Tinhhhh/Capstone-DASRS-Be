package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.Schedule;
import com.sep490.dasrsbackend.Util.TournamentSpecification;
import com.sep490.dasrsbackend.converter.TeamConverter;
import com.sep490.dasrsbackend.dto.ParticipantDTO;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.*;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.exception.TeamNotFoundException;
import com.sep490.dasrsbackend.model.payload.request.EditTournament;
import com.sep490.dasrsbackend.model.payload.request.NewTournament;
import com.sep490.dasrsbackend.model.payload.response.*;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.RoundUtilityService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TournamentServiceImpl.class);
    private final TournamentRepository tournamentRepository;
    private final RoundRepository roundRepository;
    private final MatchRepository matchRepository;
    private final ModelMapper modelMapper;
    private final TeamRepository teamRepository;
    private final TeamConverter teamConverter;
    private final CarRepository carRepository;
    private final AccountRepository accountRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final RoundUtilityService roundUtilityService;
    private final RoundServiceImpl roundService;

    private static void tournamentValidation(Date begin, Date end, int teamNumber) {
        Calendar calendar = Calendar.getInstance();

        if (begin.after(end)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "start date must be after end date.");
        }

        // Tournament bắt đầu sau ít nhất sau ngày hôm nay
        if (begin.before(calendar.getTime()) || end.before(calendar.getTime())) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Start date and end date must be after today: " + DateUtil.formatTimestamp(calendar.getTime()));
        }

        // Kiểm tra begin phải cách hiện tại ít không quá 12 tuần
        calendar.add(Calendar.WEEK_OF_YEAR, 12);
        Date maxBegin = calendar.getTime();

        if (begin.after(maxBegin)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Start date no more than 12 weeks from today.");
        }

        double atLeastDay = Math.ceil((double) teamNumber / Schedule.MAX_WORKING_HOURS);

        calendar.setTime(begin);
        calendar.add(Calendar.DAY_OF_WEEK, (int) atLeastDay);
        Date minEnd = calendar.getTime();

        calendar.setTime(begin);
        calendar.add(Calendar.WEEK_OF_YEAR, 12);
        Date maxEnd = calendar.getTime();

        if (end.before(minEnd) || end.after(maxEnd)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The tournament duration is at least " + atLeastDay + " days and no more than 12 weeks after the start date.");
        }
    }

//    private void generateAccountCar(Tournament tournament) {
//        List<Car> cars = carRepository.findCarsByEnabled();
////        List<Team> teams = teamRepository.getTeamByTournamentIdAndStatus(tournament.getId(), TeamStatus.ACTIVE);
//        List<Team> teams = null;
//        for (Team team : teams) {
//            List<Account> accounts = accountRepository.findByTeamIdAndIsLocked(team.getId(), false);
//            if (!accounts.isEmpty()) {
//                for (Account account : accounts) {
//                    for (Car car : cars) {
//                        AccountCar accountCar = AccountCar.builder().account(account).car(car).build();
//                        modelMapper.map(car, accountCar);
//                        accountCarRepository.save(accountCar);
//                    }
//                }
//            } else {
//                throw new DasrsException(HttpStatus.BAD_REQUEST, "Team " + team.getTeamName() + " has no members.");
//            }
//        }
//    }

    @Override
    public void createTournament(NewTournament newTournament) {

        if (newTournament.getTeamNumber() <= 1) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Team number must be at least 2");
        }

        //Đưa start date về 0h, end date về 23h59
        Date begin = DateUtil.convertToStartOfTheDay(newTournament.getStartDate());
        Date end = DateUtil.convertToEndOfTheDay(newTournament.getEndDate());

        tournamentValidation(begin, end, newTournament.getTeamNumber());

        Tournament tournament = Tournament.builder().tournamentName(newTournament.getTournamentName()).context(newTournament.getTournamentContext()).teamNumber(newTournament.getTeamNumber()).startDate(begin).endDate(end).status(TournamentStatus.ACTIVE).build();

        tournamentRepository.save(tournament);
//        generateAccountCar(tournament);
    }

    @Transactional
    @Override
    public void editTournament(Long tournamentId, EditTournament editTournament) {

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, tournament not found."));

        if (tournament.getStatus() == TournamentStatus.COMPLETED || tournament.getStatus() == TournamentStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, tournament has been completed or terminated.");
        }

        if (roundUtilityService.isMatchStarted(tournamentId)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, tournament has started.");
        }

        editTournament.setStartDate(DateUtil.convertToStartOfTheDay(editTournament.getStartDate()));
        editTournament.setEndDate(DateUtil.convertToEndOfTheDay(editTournament.getEndDate()));
        tournamentValidation(editTournament.getStartDate(), editTournament.getEndDate(), editTournament.getTeamNumber());
        int currentTeamNumber = tournament.getTeamNumber();
        int newTeamNumber = editTournament.getTeamNumber();
        modelMapper.map(editTournament, tournament);
        roundCheck(tournament);

        List<Team> teams = tournamentTeamRepository.findByTournamentId(tournamentId).stream().map(TournamentTeam::getTeam).distinct().toList();

        if (newTeamNumber < teams.size()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, team number must be greater than current team number.");
        }

        tournamentRepository.save(tournament);

        if (newTeamNumber != currentTeamNumber) {
            List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournamentId).stream().sorted(Comparator.comparing(Round::getTeamLimit).reversed()).toList();

            if (!rounds.isEmpty()) {
                Round round = rounds.get(0);

                if (round.getTeamLimit() >= newTeamNumber) {
                    throw new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, team number must be greater than current team number.");
                }

                //regenerate matches
                roundUtilityService.terminateMatchesToRegenerate(round.getId());
                roundUtilityService.generateMatch(round, tournament);
            }

        }


    }

    private void roundCheck(Tournament tournament) {
        List<Round> roundList = roundRepository.findAvailableRoundByTournamentId(tournament.getId()).stream().sorted(Comparator.comparing(Round::getTeamLimit).reversed()).toList();

        if (!roundList.isEmpty()) {
            Date start = roundList.get(0).getStartDate();
            Date end = roundList.get(roundList.size() - 1).getEndDate();

            if (tournament.getStartDate().after(start)) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, cannot change tournament start date to the day after first round start.");
            }
            if (tournament.getEndDate().before(end)) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Update fails, cannot change tournament end date to before last round end.");
            }
        }
    }

    @Override
    public ListTournament getAllTournaments(int pageNo, int pageSize, TournamentSort sortBy, String keyword, TournamentStatusFilter status) {

        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Specification<Tournament> spec;
        if (status == TournamentStatusFilter.ALL) {
            spec = Specification.where(TournamentSpecification.hasTournamentName(keyword));
        } else {
            TournamentStatus statusFilter = TournamentStatus.valueOf(status.getStatus());
            spec = Specification.where(TournamentSpecification.hasTournamentName(keyword)).and(TournamentSpecification.hasTournamentStatus(statusFilter));
        }

        Page<Tournament> tournamentContents = tournamentRepository.findAll(spec, pageable);
        List<Tournament> tournaments = tournamentContents.getContent();

        List<TournamentResponse> content = new ArrayList<>();

        tournaments.forEach(tournamentResponse -> {
            TournamentResponse tournament = getTournament(tournamentResponse.getId());
            tournament.setStarted(roundUtilityService.isMatchStarted(tournamentResponse.getId()));
            content.add(tournament);
        });

        return ListTournament.builder().content(content).totalPages(tournamentContents.getTotalPages()).totalElements(tournamentContents.getTotalElements()).pageNo(tournamentContents.getNumber()).pageSize(tournamentContents.getSize()).last(tournamentContents.isLast()).build();
    }

    @Override
    public TournamentResponse getTournament(Long id) {

        Tournament tournament = tournamentRepository.findById(id).orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Tournament not found."));

        List<Round> roundList = roundRepository.findByTournamentId(id);
        List<Team> teamList = tournamentTeamRepository.findByTournamentId(id).stream().map(TournamentTeam::getTeam).distinct().toList();
        List<RoundResponse> roundResponses = null;
        List<TeamTournamentResponse> teamResponses = null;
        if (!roundList.isEmpty()) {
            roundResponses = getRoundResponses(roundList);
        }

        if (!teamList.isEmpty()) {
            teamResponses = getTeamResponses(teamList);
        }

        return TournamentResponse.builder().id(tournament.getId()).tournamentName(tournament.getTournamentName()).context(tournament.getContext()).teamNumber(tournament.getTeamNumber()).status(tournament.getStatus()).startDate(DateUtil.formatTimestamp(tournament.getStartDate())).endDate(DateUtil.formatTimestamp(tournament.getEndDate())).createdDate(DateUtil.formatTimestamp(tournament.getCreatedDate())).roundList(roundResponses).teamList(teamResponses).build();

    }

    private List<TeamTournamentResponse> getTeamResponses(List<Team> teamList) {
        modelMapper.getConfiguration().setFieldMatchingEnabled(true).setFieldAccessLevel(Configuration.AccessLevel.PRIVATE).setAmbiguityIgnored(true).setSkipNullEnabled(false).setMatchingStrategy(MatchingStrategies.STRICT);

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
            roundResponse.setFinishType(round.getFinishType());
            roundResponse.setMatchTypeName(round.getMatchType().getMatchTypeName());
            roundResponse.setTournamentId(round.getTournament().getId());
            roundResponse.setScoredMethodId(round.getScoredMethod().getId());
            roundResponse.setEnvironmentId(round.getEnvironment().getId());
            roundResponse.setMatchTypeId(round.getMatchType().getId());
            roundResponse.setMapId(round.getResource().getId());
            roundResponses.add(roundResponse);
        });

        return roundResponses;
    }

    public void startTournament(Long id) {

        //kiểm tra xem tournament có tồn tại không
        Tournament tournament = tournamentRepository.findByIdAndStatus(id, TournamentStatus.ACTIVE).orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Tournament not found."));

        if (tournamentRepository.findByStatus(TournamentStatus.ACTIVE).isPresent()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "There is another tournament is active.");
        }

        //Kiểm tra có đáp ứng số ngày ko
        tournamentValidation(tournament.getStartDate(), tournament.getEndDate(), tournament.getTeamNumber());

//        List<Round> roundList = roundRepository.findByTournamentIdAndStatus(id, RoundStatus.PENDING);
//
//        if (roundList.isEmpty()) {
//            throw new DasrsException(HttpStatus.BAD_REQUEST, "Start fails, cannot start tournament without rounds.");
//        }
//
//        List<Team> teamList = teamRepository.getTeamByTournamentId(id);
//        if (teamList.size() != tournament.getTeamNumber()) {
//            throw new DasrsException(HttpStatus.BAD_REQUEST, "Start fails, cannot start tournament without enough teams.");
//        }

        tournament.setStatus(TournamentStatus.ACTIVE);
        tournamentRepository.save(tournament);

    }

    @Override
    public void terminateTournament(Long id) {

        Tournament tournament = tournamentRepository.findById(id).orElseThrow(() -> new DasrsException(HttpStatus.NOT_FOUND, "Tournament not found."));

        if (tournament.getStatus() == TournamentStatus.COMPLETED || tournament.getStatus() == TournamentStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Can't perform any actions, Tournament has been completed or terminated.");
        }

        tournament.setStatus(TournamentStatus.TERMINATED);

        //Terminate các round, match chưa hoàn thành
        terminateRoundByTournamentId(id);

        tournament.setStatus(TournamentStatus.TERMINATED);
        tournamentRepository.save(tournament);

        List<TournamentTeam> tournamentTeams = tournamentTeamRepository.findByTournamentId(id);
        if (!tournamentTeams.isEmpty()) {
            tournamentTeams.forEach(team -> {
                team.setActive(false);
                tournamentTeamRepository.save(team);
            });
        }
    }


    public void terminateRoundByTournamentId(Long id) {

        List<Round> roundList = roundRepository.findValidRoundByTournamentId(id);

        for (Round round : roundList) {
            round.setStatus(RoundStatus.TERMINATED);

            List<Match> matchList = matchRepository.findByRoundId(round.getId());
            for (Match match : matchList) {
                if (match.getStatus() == MatchStatus.PENDING) {
                    match.setStatus(MatchStatus.TERMINATED);
                    matchRepository.save(match);
                }
            }
            roundRepository.save(round);
        }

    }

    @Override
    public void extendTournamentEndDate(Long id, LocalDateTime day) {

        day = DateUtil.convertToLocalDateTime(DateUtil.convertToEndOfTheDay(DateUtil.convertToDate(day)));

        Tournament tournament = tournamentRepository.findById(id).orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Tournament not found."));

        if (tournament.getStatus() != TournamentStatus.ACTIVE) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Tournament must be active to extend end date.");
        }

        LocalDateTime oldEndDate = DateUtil.convertToLocalDateTime(tournament.getEndDate());

        if (day.isBefore(oldEndDate)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Tournament endDate cannot change to before current end date: " + DateUtil.formatTimestamp(tournament.getEndDate()));
        }

        if (day.isAfter(oldEndDate.plusDays(14))) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Tournament extend endDate cannot change > 14 days at once.");
        }

        tournament.setEndDate(DateUtil.convertToDate(day));
        tournamentRepository.save(tournament);
    }

    @Scheduled(cron = "1 0 0 * * ?")
    public void endTournament() {
        logger.info("Detecting end tournament task is running.");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();

        //Kiểm tra xem có tournament nào kết thúc ko
        List<Tournament> tournament = tournamentRepository.findByStatusAndEndDateBefore(TournamentStatus.ACTIVE, date);
        if (!tournament.isEmpty()) {
            for (Tournament t : tournament) {
                logger.info("Found a tournament that has reached the end date.");

                t.setStatus(TournamentStatus.COMPLETED);
                tournamentRepository.save(t);
                logger.info("Tournament completed successfully. Tournament Id: {}", t.getId());

                List<TournamentTeam> tournamentTeams = tournamentTeamRepository.findByTournamentId(t.getId()).stream().filter(TournamentTeam::isActive).toList();

                if (!tournamentTeams.isEmpty()) {
                    tournamentTeams.forEach(team -> {
                        team.setActive(false);
                        tournamentTeamRepository.save(team);
                    });

                } else {
                    logger.error("TournamentTeam has no records.");
                }

            }
        }

        logger.info("Detecting end tournament task is completed.");
    }

    @Override
    public List<TeamTournamentDetails> getTeamsByTournamentId(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Tournament not found"));

        List<TournamentTeam> tournamentTeams = tournamentTeamRepository.findByTournamentId(tournamentId);

        if (tournamentTeams.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. No data found");
        }

        return tournamentTeams.stream().collect(Collectors.groupingBy(TournamentTeam::getTeam)).entrySet().stream().map(entry -> {
            Team team = entry.getKey();
            List<TournamentTeam> tournamentTeamMember = entry.getValue();

            //Map team -> TeamTournamentDetails
            TeamTournamentDetails result = modelMapper.map(team, TeamTournamentDetails.class);

            //Map Account -> participantDTO
            List<ParticipantDTO> members = tournamentTeamMember.stream().map(TournamentTeam::getAccount).map(account -> modelMapper.map(account, ParticipantDTO.class)).toList();

            result.setTeamMembers(members);
            return result;
        }).toList();
    }

    @Override
    public void registerTeamToTournament(Long tournamentId, Long teamId) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Tournament not found"));

            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Team not found"));

            if (team.getStatus() != TeamStatus.ACTIVE) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Team is not active");
            }

            List<Account> teamMembers = team.getAccountList();
            if (teamMembers.size() < 4 || teamMembers.size() > 5) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Team must have between 4 and 5 members to register");
            }

            List<Tournament> activeTournaments = tournamentTeamRepository.findActiveTournamentsByTeamId(teamId);
            if (!activeTournaments.isEmpty()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST,
                        "The team is already participating in an active tournament: " +
                                activeTournaments.get(0).getTournamentName());
            }

            List<Team> teamsInTournament = tournamentTeamRepository.findByTournamentId(tournamentId).stream()
                    .map(TournamentTeam::getTeam)
                    .distinct()
                    .toList();

            if (tournament.getTeamNumber() <= teamsInTournament.size()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "The tournament has reached the maximum number of teams");
            }

            List<Account> accounts = accountRepository.findByTeamId(teamId);
            for (Account account : accounts) {
                if (!account.isLocked()) {
                    TournamentTeam tournamentTeam = new TournamentTeam();
                    tournamentTeam.setTournament(tournament);
                    tournamentTeam.setTeam(team);
                    tournamentTeam.setAccount(account);
                    tournamentTeam.setActive(true);
                    tournamentTeamRepository.save(tournamentTeam);
                }
            }

            roundUtilityService.injectTeamToMatchTeam(tournamentId);

        } catch (DasrsException ex) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            throw new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while registering the team to the tournament.");
        }
    }


    @Override
    public List<TournamentByTeamResponse> getTournamentsByTeamId(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team with ID " + teamId + " not found"));

        List<Tournament> tournaments = tournamentTeamRepository.findTournamentsByTeamId(teamId);

        if (tournaments.isEmpty()) {
            throw new IllegalStateException("No tournaments found for team: " + team.getTeamName());
        }

        return tournaments.stream().map(tournament -> {
            TournamentByTeamResponse response = new TournamentByTeamResponse();
            response.setId(tournament.getId());
            response.setTournamentName(tournament.getTournamentName());
            response.setContext(tournament.getContext());
            response.setTeamNumber(tournament.getTeamNumber());
            response.setStatus(tournament.getStatus());
            response.setStarted(tournament.getStartDate() != null);
            response.setStartDate(tournament.getStartDate() != null ? tournament.getStartDate().toString() : null);
            response.setEndDate(tournament.getEndDate() != null ? tournament.getEndDate().toString() : null);
            response.setCreatedDate(tournament.getCreatedDate().toString());
            return response;
        }).toList();
    }

    @Override
    public TodayDashboard getDashboardByRange(LocalDateTime startDate, LocalDateTime endDate) {

        Date start = DateUtil.convertToStartOfTheDay(DateUtil.convertToDate(startDate));
        Date end = DateUtil.convertToEndOfTheDay(DateUtil.convertToDate(endDate));

        if (end.before(start)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "End date must be after start date.");
        }

        TodayDashboard todayDashboard = getDashboard(start, end);

        return todayDashboard;
    }

    private TodayDashboard getDashboard(Date start, Date end) {
        int tournamentCount = tournamentRepository.countByCreatedDateBetween(start, end);
        int roundCount = roundRepository.countByCreatedDateBetween(start, end);
        int matchCount = matchRepository.countByCreatedDateBetween(start, end);
        int teamCount = teamRepository.countByCreatedDateBetween(start, end);
        int accountCount = accountRepository.countByCreatedDateBetween(start, end);

        TodayDashboard todayDashboard = new TodayDashboard();
        todayDashboard.setTournamentCount(tournamentCount);
        todayDashboard.setRoundCount(roundCount);
        todayDashboard.setMatchCount(matchCount);
        todayDashboard.setTeamCount(teamCount);
        todayDashboard.setPlayerCount(accountCount);
        todayDashboard.setStartDate(DateUtil.formatTimestamp(start));
        todayDashboard.setEndDate(DateUtil.formatTimestamp(end));
        return todayDashboard;
    }

    @Override
    public MonthlyDashboard getMonthlyDashboardByRange(LocalDateTime startDate, LocalDateTime endDate) {
        Map<Integer, List<LocalDateTime>> result = new LinkedHashMap<>();

        Date start = DateUtil.convertToStartOfTheDay(DateUtil.convertToDate(startDate));
        Date end = DateUtil.convertToEndOfTheDay(DateUtil.convertToDate(endDate));

        if (end.before(start)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "End date must be after start date.");
        }

        LocalDate startLocalDate = DateUtil.convertToLocalDateTime(start).toLocalDate();
        LocalDate endLocalDate = DateUtil.convertToLocalDateTime(end).toLocalDate();


        // Tìm thứ Hai đầu tiên chứa start
        LocalDate currentStart = startLocalDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int weekIndex = 1;

        while (!currentStart.isAfter(endLocalDate)) {
            //Tìm ngày cuối cùng của tuần này
            LocalDate currentEnd = currentStart.plusDays(6);

            //Kiểm tra ngày cuối cùng tuần này có vượt quá end date không
            if (currentEnd.isAfter(endLocalDate)) {
                currentEnd = endLocalDate;
            }

            List<LocalDateTime> range = List.of(
                    currentStart.atStartOfDay(),
                    currentEnd.atTime(LocalTime.MAX)
            );

            result.put(weekIndex++, range);
            currentStart = currentStart.plusWeeks(1);
        }

        MonthlyDashboard monthlyDashboard = new MonthlyDashboard();
        List<TodayDashboard> todayDashboards = new ArrayList<>();
        for (Map.Entry<Integer, List<LocalDateTime>> entry : result.entrySet()) {
            int week = entry.getKey();
            List<LocalDateTime> dateRange = entry.getValue();
            Date startRange = DateUtil.convertToDate(dateRange.get(0));
            Date endRange = DateUtil.convertToDate(dateRange.get(1));
            TodayDashboard todayDashboard = getDashboard(startRange, endRange);
            monthlyDashboard.setWeek(week);
            todayDashboards.add(todayDashboard);
        }

        monthlyDashboard.setWeeklyDashboard(todayDashboards);

        return monthlyDashboard;
    }


}
