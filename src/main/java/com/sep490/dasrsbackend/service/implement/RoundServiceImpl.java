package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.RoundSpecification;
import com.sep490.dasrsbackend.Util.Schedule;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.*;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.request.EditRound;
import com.sep490.dasrsbackend.model.payload.request.NewRound;
import com.sep490.dasrsbackend.model.payload.response.*;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.RoundService;
import com.sep490.dasrsbackend.service.RoundUtilityService;
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
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class RoundServiceImpl implements RoundService {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RoundServiceImpl.class);
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
    private final TournamentTeamRepository tournamentTeamRepository;
    private final RoundUtilityService roundUtilityService;
    private final MatchServiceImpl matchServiceImpl;

    @Transactional
    @Override
    public void newRound(NewRound newRound) {

        newRound.setStartDate(DateUtil.convertToStartOfTheDay(newRound.getStartDate()));
        newRound.setEndDate(DateUtil.convertToEndOfTheDay(newRound.getEndDate()));

        Tournament tournament = tournamentRepository.findById(newRound.getTournamentId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Tournament not found"));

        MatchType matchType = matchTypeRepository.findByIdAndStatus(newRound.getMatchTypeId(), MatchTypeStatus.ACTIVE)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Match Type not found"));

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

        ScoredMethod sc = ScoredMethod.builder()
                .lap(newRound.getLap())
                .collision(newRound.getCollision())
                .totalRaceTime(newRound.getTotalRaceTime())
                .offTrack(newRound.getOffTrack())
                .assistUsageCount(newRound.getAssistUsageCount())
                .averageSpeed(newRound.getAverageSpeed())
                .totalDistance(newRound.getTotalDistance())
                .matchFinishType(newRound.getFinishType())
                .status(ScoredMethodStatus.ACTIVE)
                .build();

        sc = scoredMethodRepository.save(sc);

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
                .scoredMethod(sc)
                .environment(environment)
                .resource(resource)
                .build();
        roundRepository.save(round);

        updateLatestRound(tournament);
        roundUtilityService.generateMatch(round, tournament);
//        generateLeaderboard(round);
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

        Environment environment = environmentRepository.findByIdAndStatus(request.getEnvironmentId(), EnvironmentStatus.ACTIVE)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Environment not found"));

        Resource map = resourceRepository.findByIdAndIsEnable(request.getResourceId(), true)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Map not found"));

        if (map.getResourceType() != ResourceType.MAP) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Resource invalid, The resource need to be a map");
        }

        if (roundUtilityService.isMatchStartedForRound(round.getId())) {
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

        //Đổi match type hoặc start date thì phải generate lại match
        boolean flag = false;

        if (round.getMatchType().getId() != request.getMatchTypeId()) {
            flag = true;
        }

        if (round.getStartDate().getTime() != request.getStartDate().getTime()) {
            flag = true;
        }

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setAmbiguityIgnored(true)
                .setSkipNullEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        Long scId = round.getScoredMethod().getId();
        ScoredMethod sc = round.getScoredMethod();
        modelMapper.map(request, sc);
        sc.setId(scId);
        modelMapper.map(request, round);
        round.setScoredMethod(sc);

        round.setMatchType(matchType);
        round.setScoredMethod(sc);
        round.setEnvironment(environment);
        round.setResource(map);

        roundRepository.save(round);
        if (flag) {
            roundUtilityService.terminateMatchesToRegenerate(round.getId());
            roundUtilityService.generateMatch(round, tournament);
        }
    }

    @Transactional
    @Override
    public void terminateRound(Long id) {

        Round round = roundRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

        if (round.getStatus() != RoundStatus.ACTIVE) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Terminate fails, this round has been completed or terminated");
        }

        //Terminate các round, match chưa hoàn thành
        terminateMatchesByRound(round);

        round.setStatus(RoundStatus.TERMINATED);
        round.setLatest(false);
        roundRepository.save(round);
        updateLatestRound(round.getTournament());
    }

    @Override
    public RoundResponseDetails findRoundByRoundId(Long id) {

        Round round = roundRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

        modelMapper.getConfiguration().setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setAmbiguityIgnored(true)
                .setSkipNullEnabled(false)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        RoundResponseDetails content = modelMapper.map(round, RoundResponseDetails.class);

        content.setStartDate(DateUtil.formatTimestamp(round.getStartDate()));
        content.setEndDate(DateUtil.formatTimestamp(round.getEndDate()));
        content.setCreateDate(DateUtil.formatTimestamp(round.getCreatedDate()));
        content.setMatchTypeName(round.getMatchType().getMatchTypeName());
        content.setTournamentId(round.getTournament().getId());
        content.setScoredMethodId(round.getScoredMethod().getId());
        content.setEnvironmentId(round.getEnvironment().getId());
        content.setMatchTypeId(round.getMatchType().getId());
        content.setMapId(round.getResource().getId());

        List<MatchResponse> matchResponses = new ArrayList<>();
        List<Match> matches = matchRepository.findByRoundId(round.getId()).stream()
                .filter(match -> match.getStatus() != MatchStatus.TERMINATED).toList();

        if (!matches.isEmpty()) {
            matches.forEach(match -> {
                MatchResponse matchResponse = matchServiceImpl.getMatchResponse(match);
                matchResponses.add(matchResponse);
            });
        }
        content.setMatchList(matchResponses);
        return content;

    }

    @Override
    public List<RoundResponseDetails> findRoundByTournamentId(Long id) {
        // by admin, organizer
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Tournament not found"));

        List<Round> rounds = roundRepository.findByTournamentId(tournament.getId());
        List<RoundResponseDetails> roundResponses = new ArrayList<>();
        rounds.forEach(round -> {
            RoundResponseDetails roundResponse = findRoundByRoundId(round.getId());
            roundResponses.add(roundResponse);
        });

        return roundResponses;
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
    public GetRoundsByTeamResponse getRoundsByTeamIdAndTournamentId(Long teamId, Long tournamentId, int pageNo, int pageSize, RoundSort sortBy, String keyword) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Round> spec = Specification.where(RoundSpecification.belongsToTeam(teamId))
                .and(RoundSpecification.belongsToTournament(tournamentId))
                .and(RoundSpecification.hasKeyword(keyword));

        Page<Round> roundsPage = roundRepository.findAll(spec, pageable);
        List<Round> rounds = roundsPage.getContent();

        List<GetTeamRoundResponse> roundResponses = rounds.stream()
                .map(round -> new GetTeamRoundResponse(
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

        return GetRoundsByTeamResponse.builder()
                .rounds(roundResponses)
                .totalPages(roundsPage.getTotalPages())
                .totalElements(roundsPage.getTotalElements())
                .pageNo(roundsPage.getNumber())
                .pageSize(roundsPage.getSize())
                .last(roundsPage.isLast())
                .build();
    }


    @Override
    public ListRoundResponseDetails findAllRounds(int pageNo, int pageSize, RoundSort sortBy, String keyword, RoundStatusFilter status) {

        Sort sort = Sort.by(sortBy.getField()).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Specification<Round> spec;
        if (status == RoundStatusFilter.ALL) {
            spec = Specification.where(RoundSpecification.hasRoundName(keyword));
        } else {
            spec = Specification.where(RoundSpecification.hasRoundName(keyword).and(
                    RoundSpecification.hasStatus(RoundStatus.valueOf(status.getStatus()))
            ));
        }

        Page<Round> roundPage = roundRepository.findAll(spec, pageable);

        List<RoundResponseDetails> roundResponses = new ArrayList<>();
        roundPage.getContent().forEach(round -> {
            RoundResponseDetails roundResponse = findRoundByRoundId(round.getId());
            roundResponses.add(roundResponse);
        });

        ListRoundResponseDetails listRoundResponses = new ListRoundResponseDetails();
        listRoundResponses.setTotalPages(roundPage.getTotalPages());
        listRoundResponses.setTotalElements(roundPage.getTotalElements());
        listRoundResponses.setPageNo(roundPage.getNumber());
        listRoundResponses.setPageSize(roundPage.getSize());
        listRoundResponses.setLast(roundPage.isLast());
        listRoundResponses.setContent(roundResponses);

        return listRoundResponses;
    }

    @Override
    public ListRoundResponseDetails findAllRoundsByDate(int pageNo, int pageSize, RoundSort sortBy, String keyword, LocalDateTime start, LocalDateTime end) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Specification<Round> spec = Specification
                .where(RoundSpecification.hasKeyword(keyword))
                .and(RoundSpecification.betweenStartAndEndDate(start, end)
                        .and(RoundSpecification.hasStatus(RoundStatus.ACTIVE))
                );

        Page<Round> roundPage = roundRepository.findAll(spec, pageable);
        List<RoundResponseDetails> roundResponses = new ArrayList<>();
        roundPage.getContent().forEach(round -> {
            if (round.getTournament().getStatus() == TournamentStatus.ACTIVE) {
                RoundResponseDetails roundResponse = findRoundByRoundId(round.getId());
                roundResponses.add(roundResponse);
            }
        });

        ListRoundResponseDetails listRoundResponses = new ListRoundResponseDetails();
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
        try {
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

            List<Account> accounts = accountRepository.findByTeamId(teamId);

            for (Account account : accounts) {
                if (!account.isLocked()) {
                    TournamentTeam tournamentTeam = new TournamentTeam();
                    tournamentTeam.setTournament(tournament);
                    tournamentTeam.setTeam(team);
                    tournamentTeam.setAccount(account);
                    tournamentTeamRepository.save(tournamentTeam);
                }
            }

            roundUtilityService.injectTeamToMatchTeam(tournamentId);

        } catch (DasrsException ex) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            throw new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while injecting the team to the tournament.");
        }
    }

    @Override
        public void extendRoundEndDate(Long id, LocalDateTime newEndDate) {

        newEndDate = DateUtil.convertToLocalDateTime(DateUtil.convertToEndOfTheDay(DateUtil.convertToDate(newEndDate)));

        Round round = roundRepository.findById(id).orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Round not found."));

        if (round.getStatus() == RoundStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails. Terminated round cannot be extended.");
        }

        LocalDateTime oldEndDate = DateUtil.convertToLocalDateTime(round.getEndDate());

        if (newEndDate.isBefore(oldEndDate)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Round end date cannot change to before current end date: " + DateUtil.formatTimestamp(round.getEndDate()));
        }

        if (newEndDate.isAfter(oldEndDate.plusDays(7))) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Round extend end date cannot change > 7 days at once.");
        }

        Tournament tournament = round.getTournament();

        if (tournament.getStatus() != TournamentStatus.ACTIVE) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The tournament is completed or terminated, can't edit round");
        }

        if (newEndDate.isAfter(DateUtil.convertToLocalDateTime(tournament.getEndDate()))) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Round end date cannot change to the date after current tournament end date: " + DateUtil.formatTimestamp(tournament.getEndDate()));
        }

        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournament.getId()).stream()
                .sorted(Comparator.comparing(Round::getTeamLimit).reversed()).toList();

        for (int i = 0; i < rounds.size(); i++) {
            if (rounds.get(i).getId() == round.getId()) {
                //Nếu là round cuối cùng thì không cần kiểm tra
                if (round.isLast()) {
                    return;
                }

                //Nếu không phải round cuối cùng thì kiểm tra round sau
                if (i + 1 < rounds.size()) {
                    Round nextRound = rounds.get(i + 1);
                    if (nextRound.getStartDate().before(DateUtil.convertToDate(newEndDate))) {
                        throw new DasrsException(HttpStatus.BAD_REQUEST, "Round end date cannot change to the date after next round start date: " + DateUtil.formatTimestamp(nextRound.getStartDate()));
                    }

                    if (roundUtilityService.isMatchStartedForRound(nextRound.getId())) {
                        throw new DasrsException(HttpStatus.BAD_REQUEST, "Cannot extend round, there are matches in the next round that have started");
                    }

                }
            }
        }

        round.setEndDate(DateUtil.convertToDate(newEndDate));
        round.setStatus(RoundStatus.ACTIVE);
        roundRepository.save(round);
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
        long result = daysBetween + 1;

        if (result < Math.ceil(matches / Schedule.MAX_WORKING_HOURS)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "The round schedule is invalid, the round must have at least " + matches +
                            " working hours, at least " + Math.ceil(matches / Schedule.MAX_WORKING_HOURS) + " days to create matches");
        }

    }

    private void editRoundValidation(EditRound editRound, Tournament tournament) {

        if (tournament.getStatus() != TournamentStatus.ACTIVE) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The tournament is completed or terminated, can't edit round");
        }

        //Find round dđể update thì chỉ tìm round status = pending và active
        List<Round> rounds = roundRepository.findValidRoundByTournamentId(tournament.getId()).stream()
                .sorted(Comparator.comparing(Round::getTeamLimit).reversed()).toList();

        Round round = roundRepository.findById(editRound.getId())
                .orElseThrow(() -> new DasrsException(HttpStatus.BAD_REQUEST, "Round not found"));

        if (!round.isLatest()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Edit round failed, this round is not the latest round");
        }

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

            if (!rounds.get(rounds.size() - 1).isLatest()) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, cannot edit round that is not the latest round");
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

    private void terminateMatchesByRound(Round round) {
        List<Match> matches = matchRepository.findByRoundId(round.getId()).stream().
                filter(match -> match.getStatus() == MatchStatus.PENDING)
                .toList();

        for (Match match : matches) {
            match.setStatus(MatchStatus.TERMINATED);
            List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());
            for (MatchTeam matchTeam : matchTeams) {
                if (matchTeam.getStatus() == MatchTeamStatus.UNASSIGNED) {
                    matchTeam.setStatus(MatchTeamStatus.TERMINATED);
                }
                matchTeamRepository.save(matchTeam);
            }
            matchRepository.save(match);
        }
    }

    //second, minute, hour, day, month, year
    //* = every
    //@Scheduled(cron = "5 * * * * ?")
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
        List<Round> roundEnd = roundRepository.findByStatusAndEndDateBefore(RoundStatus.ACTIVE, date);
        if (!roundEnd.isEmpty()) {
            for (Round round : roundEnd) {
                logger.info("Found a round that reach end date");
                Tournament tournament = round.getTournament();
                if (tournament.getStatus() == TournamentStatus.ACTIVE) {
                    round.setStatus(RoundStatus.COMPLETED);
                    roundRepository.save(round);
                    logger.info("Change round status to completed. Round id: {}", round.getId());
                }
                roundUtilityService.injectTeamToMatchTeam(tournament.getId());
            }
        }

        logger.info("Detecting round end task is completed");
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
