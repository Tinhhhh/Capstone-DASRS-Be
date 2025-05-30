package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.*;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.response.*;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.LeaderboardService;
import com.sep490.dasrsbackend.service.RoundUtilityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final RoundRepository roundRepository;
    private final ModelMapper modelMapper;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final TournamentRepository tournamentRepository;
    private final RoundUtilityService roundUtilityService;
    private final ScoreAttributeRepository scoreAttributeRepository;

    private static Pageable getPageable(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(pageNo, pageSize, sort);
    }

    @Override
    public void updateLeaderboard(Long roundId) {

        Round round = roundRepository.findById(roundId).orElseThrow(
                () -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, round not found")
        );

        //Xếp hạng lại leaderboard
        //Lấy lại danh sách leaderboard
        List<Leaderboard> lbs = leaderboardRepository.findByRoundId(round.getId());

        //Sort leaderboard
        lbs.sort((l1,l2) -> {

            //So sánh điểm của team
            int scoreCompare = Double.compare(l2.getTeamScore(), l1.getTeamScore());
            if (scoreCompare == 0) {

                if (l2.getTeamScore() == 0 && l1.getTeamScore() == 0) {
                    return l1.getTeam().getTeamName().compareToIgnoreCase(l2.getTeam().getTeamName());
                }

                //Lấy matchteam của Team theo match trong round này.
                List<MatchTeam> matchTeams = matchRepository.findByRoundId(round.getId()).stream()
                        .filter(match -> match.getStatus() == MatchStatus.FINISHED)
                        .flatMap(match -> match.getMatchTeamList().stream()).toList();

                //Lấy matchteam của team theo match trong round này
                List<MatchTeam> team1 = new ArrayList<>(matchTeams.stream().filter(matchTeam -> Objects.equals(matchTeam.getTeam().getId(), l1.getTeam().getId())).toList());
                List<MatchTeam> team2 = new ArrayList<>(matchTeams.stream().filter(matchTeam -> Objects.equals(matchTeam.getTeam().getId(), l2.getTeam().getId())).toList());

                //Trường hợp có điểm bằng nhau thì so sánh thời gian nếu thi đầu theo lap
                if (round.getFinishType() == FinishType.LAP){

                    //Sắp xếp matchteam theo fastestlaptime của từng team
                    team1.sort((mt1,mt2) -> {
                        ScoreAttribute attr1 = mt1.getScoreAttribute();
                        ScoreAttribute attr2 = mt2.getScoreAttribute();

                        if (attr1 == null && attr2 == null) return 0;
                        if (attr1 == null) return 1; // attr1 null  đứng sau
                        if (attr2 == null) return -1; // attr2 null  đứng sau

                        return Double.compare(attr1.getFastestLapTime(), attr2.getFastestLapTime());
                    });

                    team2.sort((mt1,mt2) -> {
                        ScoreAttribute attr1 = mt1.getScoreAttribute();
                        ScoreAttribute attr2 = mt2.getScoreAttribute();

                        if (attr1 == null && attr2 == null) return 0;
                        if (attr1 == null) return 1; // attr1 null  đứng sau
                        if (attr2 == null) return -1; // attr2 null  đứng sau

                        return Double.compare(attr1.getFastestLapTime(), attr2.getFastestLapTime());
                    });

                    //So sánh fastestlaptime của từng team
                    return Double.compare(team1.get(0).getScoreAttribute().getFastestLapTime(), team2.get(0).getScoreAttribute().getFastestLapTime());
                }

                //Trường hợp có điểm bằng nhau thì so sánh thời gian nếu thi đấu theo time
                if (round.getFinishType() == FinishType.TIME){

                    //Sắp xếp matchteam theo fastestlaptime của từng team
                    team1.sort((mt1,mt2) -> {
                        ScoreAttribute attr1 = mt1.getScoreAttribute();
                        ScoreAttribute attr2 = mt2.getScoreAttribute();

                        if (attr1 == null && attr2 == null) return 0;
                        if (attr1 == null) return 1; // attr1 null  đứng sau
                        if (attr2 == null) return -1; // attr2 null  đứng sau

                        return Double.compare(attr2.getTotalDistance(), attr1.getTotalDistance());
                    });

                    team2.sort((mt1,mt2) -> {
                        ScoreAttribute attr1 = mt1.getScoreAttribute();
                        ScoreAttribute attr2 = mt2.getScoreAttribute();

                        if (attr1 == null && attr2 == null) return 0;
                        if (attr1 == null) return 1; // attr1 null  đứng sau
                        if (attr2 == null) return -1; // attr2 null  đứng sau

                        return Double.compare(attr2.getTotalDistance(), attr1.getTotalDistance());
                    });

                    return Double.compare(team2.get(0).getScoreAttribute().getTotalDistance(), team1.get(0).getScoreAttribute().getTotalDistance());
                }
            }

            return scoreCompare;
        });

        int rank = 1;
        for (Leaderboard leaderboard : lbs) {
            leaderboard.setRanking(rank++);
        }

        leaderboardRepository.saveAll(lbs);

        //Kiểm tra xem round đã hoàn thành chưa
        if (round.getStatus() == RoundStatus.ACTIVE) {
            List<Match> matches = matchRepository.findByRoundId(round.getId()).stream()
                    .filter(match -> match.getStatus() != MatchStatus.TERMINATED).toList();

            boolean isCompleted = true;
            for (Match match : matches) {
                if (match.getStatus() == MatchStatus.PENDING) {
                    isCompleted = false;
                    break;
                }
            }

            if (isCompleted) {
                round.setStatus(RoundStatus.COMPLETED);
                roundUtilityService.injectTeamToMatchTeam(round.getTournament().getId());
                roundRepository.save(round);
            }
        }

    }

    @Override
    public LeaderboardResponseForRound getLeaderboardByRoundId(Long roundId, int pageNo, int pageSize, String sortBy, String sortDir) {

        Round round = roundRepository.findById(roundId).orElseThrow(
                () -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, round not found")
        );

        if (round.getStatus() == RoundStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, round is terminated");
        }

//        if (round.getStatus() == RoundStatus.PENDING) {
//            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, round is pending, please wait for the round to active or completed");
//        }

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Leaderboard> leaderboards = leaderboardRepository.findByRoundId(round.getId(), pageable);

        if (leaderboards.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Leaderboard not found, please check if the round is valid");
        }

        LeaderboardResponseForRound leaderboardList = new LeaderboardResponseForRound();

        List<LeaderboardData> leaderboardData = leaderboards.stream()
                .map(leaderboard -> {
                    LeaderboardData lbr = modelMapper.map(leaderboard, LeaderboardData.class);
                    Team team = leaderboard.getTeam();
                    lbr.setTeamId(leaderboard.getTeam().getId());
                    lbr.setTeamName(team.getTeamName());
                    lbr.setTeamTag(team.getTeamTag());
                    lbr.setCreatedDate(DateUtil.formatTimestamp(leaderboard.getCreatedDate()));
                    return lbr;
                })
                .toList();

        leaderboardList.setRoundId(round.getId());
        leaderboardList.setRoundName(round.getRoundName());
        leaderboardList.setFinishType(round.getFinishType());

        leaderboardList.setContent(leaderboardData);
        leaderboardList.setPageNo(leaderboards.getNumber());
        leaderboardList.setPageSize(leaderboards.getSize());
        leaderboardList.setTotalElements(leaderboards.getTotalElements());
        leaderboardList.setTotalPages(leaderboards.getTotalPages());

        Result result = getFastestLapTimeAndTopSpeed(round);

        leaderboardList.setFastestLapTime(result.fastestLapTime());
        leaderboardList.setTopSpeed(result.topSpeed());

        return leaderboardList;
    }

    @Override
    public List<LeaderboardData> getLeaderboardByTeamId(Long teamId, int pageNo, int pageSize, String sortBy, String sortDir) {

        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, team not found")
        );

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Leaderboard> leaderboards = leaderboardRepository.findByTeamId(team.getId(), pageable);

        if (leaderboards.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Leaderboard not found");
        }

        List<LeaderboardData> leaderboardData = leaderboards.stream()
                .map(leaderboard -> {
                    LeaderboardData lbr = modelMapper.map(leaderboard, LeaderboardData.class);
                    lbr.setRoundId(leaderboard.getRound().getId());
                    lbr.setRoundName(leaderboard.getRound().getRoundName());
                    lbr.setTournamentId(leaderboard.getRound().getTournament().getId());
                    lbr.setTournamentName(leaderboard.getRound().getTournament().getTournamentName());
                    lbr.setCreatedDate(DateUtil.formatTimestamp(leaderboard.getCreatedDate()));
                    return lbr;
                })
                .toList();

        return leaderboardData;
    }

    @Override
    public LeaderboardResponseForTournament getLeaderboardByTournamentId(Long tournamentId, int pageNo, int pageSize, String sortBy, String sortDir) {

        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(
                () -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, tournament not found")
        );

        if (tournament.getStatus() == TournamentStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, tournament is terminated");
        }

        List<LeaderboardTournament> leaderboardTournaments = new ArrayList<>();

        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournamentId);

        for (Round round : rounds) {
            LeaderboardTournament leaderboardTournament = new LeaderboardTournament();
            List<LeaderboardTournamentChild> childrenContent
                    = leaderboardRepository.findByRoundId(round.getId()).stream()
                    .map(leaderboard -> {
                        LeaderboardTournamentChild child = modelMapper.map(leaderboard, LeaderboardTournamentChild.class);
                        child.setTeamId(leaderboard.getTeam().getId());
                        child.setTeamName(leaderboard.getTeam().getTeamName());
                        child.setTeamTag(leaderboard.getTeam().getTeamTag());
                        child.setCreatedDate(DateUtil.formatTimestamp(leaderboard.getCreatedDate()));
                        return child;
                    }).toList();

            leaderboardTournament.setRoundId(round.getId());
            leaderboardTournament.setRoundName(round.getRoundName());
            leaderboardTournament.setDescription(round.getDescription());
            leaderboardTournament.setFinishType(round.getFinishType());
            leaderboardTournament.setContent(childrenContent);
            Result result = getFastestLapTimeAndTopSpeed(round);
            leaderboardTournament.setFastestLapTime(result.fastestLapTime());
            leaderboardTournament.setTopSpeed(result.topSpeed());

            leaderboardTournaments.add(leaderboardTournament);
        }

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDir);

        // Sắp xếp danh sách theo Pageable
        List<LeaderboardTournament> sortedLeaderboards = leaderboardTournaments.stream()
                .sorted(getComparator(sortBy, sortDir))
                .toList();

        // Cắt danh sách theo Pageable
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedLeaderboards.size());
        List<LeaderboardTournament> pagedList = sortedLeaderboards.subList(start, end);

        // Tạo đối tượng Page từ danh sách đã phân trang
        Page<LeaderboardTournament> leaderboard = new PageImpl<>(pagedList, pageable, leaderboardTournaments.size());

        LeaderboardResponseForTournament response = new LeaderboardResponseForTournament();
        response.setId(tournament.getId());
        response.setTournamentName(tournament.getTournamentName());
        response.setContent(leaderboard.getContent());
        response.setTotalPages(leaderboard.getTotalPages());
        response.setTotalElements(leaderboard.getTotalElements());
        response.setPageNo(leaderboard.getNumber());
        response.setPageSize(leaderboard.getSize());
        response.setLast(leaderboard.isLast());

        return response;
    }

    private Result getFastestLapTimeAndTopSpeed(Round round) {
        FastestLapTimeTeam fastestLapTime = new FastestLapTimeTeam();
        TopSpeedTeam topSpeed = new TopSpeedTeam();

        List<Match> matches = matchRepository.findByRoundId(round.getId());

        double fastestLapTimeValue = 0.0;
        double topSpeedValue = 0.0;

        for (Match match : matches) {

            List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());

            //Hết test thì cmt đoạn này lại
            for (int i = 0; i < matchTeams.size(); i++) {
                if (fastestLapTimeValue == 0.0 && matchTeams.get(i).getScoreAttribute() != null) {
                    fastestLapTimeValue = matchTeams.get(i).getScoreAttribute().getFastestLapTime();
                    fastestLapTime.setId(matchTeams.get(i).getTeam().getId());
                    fastestLapTime.setTeamName(matchTeams.get(i).getTeam().getTeamName());
                    fastestLapTime.setTeamTag(matchTeams.get(i).getTeam().getTeamTag());
                    fastestLapTime.setAccountId(matchTeams.get(i).getAccount().getAccountId());
                    fastestLapTime.setFullName(matchTeams.get(i).getAccount().fullName());
                    fastestLapTime.setFastestLapTime(fastestLapTimeValue);
                    break;
                }
            }


            for (MatchTeam matchTeam : matchTeams) {

                if (matchTeam.getScoreAttribute() == null) {
                    continue;
                }

                if (fastestLapTimeValue > matchTeam.getScoreAttribute().getFastestLapTime()) {
                    fastestLapTimeValue = matchTeam.getScoreAttribute().getFastestLapTime();
                    fastestLapTime.setId(matchTeam.getTeam().getId());
                    fastestLapTime.setTeamName(matchTeam.getTeam().getTeamName());
                    fastestLapTime.setTeamTag(matchTeam.getTeam().getTeamTag());
                    fastestLapTime.setAccountId(matchTeam.getAccount().getAccountId());
                    fastestLapTime.setFullName(matchTeam.getAccount().fullName());
                    fastestLapTime.setFastestLapTime(fastestLapTimeValue);
                }

                if (topSpeedValue < matchTeam.getScoreAttribute().getTopSpeed()) {
                    topSpeedValue = matchTeam.getScoreAttribute().getTopSpeed();
                    topSpeed.setId(matchTeam.getTeam().getId());
                    topSpeed.setTeamName(matchTeam.getTeam().getTeamName());
                    topSpeed.setTeamTag(matchTeam.getTeam().getTeamTag());
                    topSpeed.setAccountId(matchTeam.getAccount().getAccountId());
                    topSpeed.setFullName(matchTeam.getAccount().fullName());
                    topSpeed.setTopSpeed(topSpeedValue);
                }

            }
        }
        Result result = new Result(fastestLapTime, topSpeed);
        return result;
    }

    private Comparator<? super LeaderboardTournament> getComparator(String sortBy, String sortDir) {

        Comparator<LeaderboardTournament> comparator;

        switch (sortBy) {
            case "roundId":
                comparator = Comparator.comparing(LeaderboardTournament::getRoundId);
                break;
            case "finishType":
                comparator = Comparator.comparing(LeaderboardTournament::getFinishType);
                break;
            case "fastestLapTime":
                comparator = Comparator.comparing(l -> l.getFastestLapTime() != null ? l.getFastestLapTime().getId() : Long.MAX_VALUE);
                break;
            case "topSpeed":
                comparator = Comparator.comparing(l -> l.getTopSpeed() != null ? l.getTopSpeed().getId() : Long.MAX_VALUE);
                break;
            default:
                comparator = Comparator.comparing(LeaderboardTournament::getRoundId);
                break;
        }

        return sortDir.equalsIgnoreCase("desc") ? comparator.reversed() : comparator;
    }

    private record Result(FastestLapTimeTeam fastestLapTime, TopSpeedTeam topSpeed) {
    }

    @Override
    public LeaderboardWithTeamInfoResponse getLeaderboardWithTeamInfoByRoundId(Long roundId, int pageNo, int pageSize, String sortBy, String sortDir) {

        Round round = roundRepository.findById(roundId).orElseThrow(
                () -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, round not found")
        );

        if (round.getStatus() == RoundStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, round is terminated");
        }

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Leaderboard> leaderboards = leaderboardRepository.findByRoundId(round.getId(), pageable);

        if (leaderboards.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Leaderboard not found, please check if the round is valid");
        }

        List<LeaderboardDataWithTeamInfo> leaderboardData = leaderboards.stream()
                .map(leaderboard -> {
                    LeaderboardDataWithTeamInfo data = new LeaderboardDataWithTeamInfo();
                    data.setId(leaderboard.getId());
                    data.setRanking(leaderboard.getRanking());
                    data.setTeamScore(leaderboard.getTeamScore());
                    data.setCreatedDate(DateUtil.formatTimestamp(leaderboard.getCreatedDate()));

                    Team team = leaderboard.getTeam();
                    data.setTeamId(team.getId());
                    data.setTeamName(team.getTeamName());
                    data.setTeamTag(team.getTeamTag());

                    Round r = leaderboard.getRound();
                    data.setRoundId(r.getId());
                    data.setRoundName(r.getRoundName());

                    Tournament t = r.getTournament();
                    data.setTournamentId(t.getId());
                    data.setTournamentName(t.getTournamentName());

                    return data;
                })
                .toList();

        Result result = getFastestLapTimeAndTopSpeed(round);

        return new LeaderboardWithTeamInfoResponse(
                round.getId(),
                round.getRoundName(),
                round.getFinishType(),
                result.fastestLapTime(),
                result.topSpeed(),
                leaderboardData,
                leaderboards.getNumber(),
                leaderboards.getSize(),
                leaderboards.getTotalElements(),
                leaderboards.getTotalPages(),
                leaderboards.isLast()
        );
    }

    @Override
    public LeaderboardForAll getLeaderboardForAllByRoundId(Long roundId, int pageNo, int pageSize, String sortBy, String sortDir) {

        Round round = roundRepository.findById(roundId).orElseThrow(
                () -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, round not found")
        );

        if (round.getStatus() == RoundStatus.TERMINATED) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, round is terminated");
        }

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Leaderboard> leaderboards = leaderboardRepository.findByRoundId(round.getId(), pageable);

        if (leaderboards.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Leaderboard not found, please check if the round is valid");
        }

        LeaderboardForAll leaderboardList = new LeaderboardForAll();

        List<LeaderboardChildForAll> leaderboardData = leaderboards.stream()
                .map(leaderboard -> {
                    LeaderboardChildForAll lbr = modelMapper.map(leaderboard, LeaderboardChildForAll.class);
                    lbr.setTeamId(leaderboard.getTeam().getId());
                    lbr.setTeamName(leaderboard.getTeam().getTeamName());
                    lbr.setTeamTag(leaderboard.getTeam().getTeamTag());
                    lbr.setCreatedDate(DateUtil.formatTimestamp(leaderboard.getCreatedDate()));

                    List<Match> matches = matchRepository.findByRoundId(round.getId());
                    List<MatchResponseForLeaderboard> matchResponseForLeaderboards = new ArrayList<>();
                    if (!matches.isEmpty()){
                        for (Match match : matches) {
                            MatchResponseForLeaderboard matchDetails = new MatchResponseForLeaderboard();
                            List<MatchTeam> matchTeams = matchTeamRepository.findByTeamIdAndMatchId(leaderboard.getTeam().getId(), match.getId());
                            double matchScore = 0.0;
                            List<PlayerResponseForLeaderboard> players = new ArrayList<>();
                            if (!matchTeams.isEmpty()) {
                                PlayerResponseForLeaderboard player = new PlayerResponseForLeaderboard();
                                for (MatchTeam matchTeam : matchTeams) {

                                    if (matchTeam.getAccount() == null) {
                                        player.setPlayerId(null);
                                        player.setPlayerName(null);
                                    } else {
                                        player.setPlayerId(matchTeam.getAccount().getAccountId());
                                        player.setPlayerName(matchTeam.getAccount().fullName());
                                    }

                                    player.setScore(matchTeam.getScore());
                                    matchScore += matchTeam.getScore();
                                    players.add(player);
                                }


                                matchDetails.setMatchId(match.getId());
                                matchDetails.setMatchName(match.getMatchName());
                                matchDetails.setMatchType(match.getRound().getMatchType().getMatchTypeName());
                                matchDetails.setMatchForm(match.getMatchForm());
                                matchDetails.setMatchScore(matchScore);
                                matchDetails.setPlayerList(players);
                                matchResponseForLeaderboards.add(matchDetails);
                            }
                        }
                    }

                    lbr.setMatchList(matchResponseForLeaderboards);
                    return lbr;
                })
                .toList();

        leaderboardList.setRoundId(round.getId());
        leaderboardList.setRoundName(round.getRoundName());
        leaderboardList.setFinishType(round.getFinishType());

        leaderboardList.setContent(leaderboardData);
        leaderboardList.setPageNo(leaderboards.getNumber());
        leaderboardList.setPageSize(leaderboards.getSize());
        leaderboardList.setTotalElements(leaderboards.getTotalElements());
        leaderboardList.setTotalPages(leaderboards.getTotalPages());

        Result result = getFastestLapTimeAndTopSpeed(round);

        leaderboardList.setFastestLapTime(result.fastestLapTime());
        leaderboardList.setTopSpeed(result.topSpeed());

        return leaderboardList;

    }

}
