package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.model.payload.response.*;
import com.sep490.dasrsbackend.repository.*;
import com.sep490.dasrsbackend.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final RoundRepository roundRepository;
    private final ModelMapper modelMapper;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final MatchTeamRepository matchTeamRepository;

    @Override
    public void updateLeaderboard(Long roundId) {

        Round round = roundRepository.findById(roundId).orElseThrow(
                () -> new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, round not found")
        );

        List<Leaderboard> lbs = leaderboardRepository.findByRoundId(round.getId());

        lbs.sort(Comparator.comparing(Leaderboard::getTeamScore).reversed());

        int rank = 1;
        for (Leaderboard leaderboard : lbs) {
            leaderboard.setRanking(rank++);
        }

        leaderboardRepository.saveAll(lbs);
    }

    @Override
    public LeaderboardResponseForRound getLeaderboardByRoundId(Long roundId, int pageNo, int pageSize, String sortBy, String sortDir) {

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

        LeaderboardResponseForRound leaderboardList = new LeaderboardResponseForRound();

        List<LeaderboardData> leaderboardData = leaderboards.stream()
                .map(leaderboard -> {
                    LeaderboardData lbr = modelMapper.map(leaderboard, LeaderboardData.class);
                    lbr.setTeamId(leaderboard.getTeam().getId());
                    lbr.setCreatedDate(DateUtil.formatTimestamp(leaderboard.getCreatedDate()));
                    return lbr;
                })
                .toList();

        leaderboardList.setRoundId(round.getId());
        leaderboardList.setFinishType(round.getMatchType().getFinishType());

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

        return leaderboards.stream()
                .map(leaderboard -> {
                    LeaderboardData leaderboardData = modelMapper.map(leaderboard, LeaderboardData.class);
                    leaderboardData.setCreatedDate(DateUtil.formatTimestamp(leaderboard.getCreatedDate()));
                    return leaderboardData;
                })
                .collect(Collectors.toList());
    }

    private static Pageable getPageable(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(pageNo, pageSize, sort);
    }

    @Override
    public ListLeaderboardResponse getLeaderboardByTournamentId(Long tournamentId, int pageNo, int pageSize, String sortBy, String sortDir) {

        List<LeaderboardResponse> leaderboardResponseList = new ArrayList<>();

        List<Round> rounds = roundRepository.findByTournamentId(tournamentId);

        for (Round round : rounds) {
            List<Leaderboard> leaderboardPage = leaderboardRepository.findByRoundId(round.getId());

            for (Leaderboard leaderboard : leaderboardPage) {
                LeaderboardResponse leaderboardResponse = modelMapper.map(leaderboard, LeaderboardResponse.class);
                leaderboardResponse.setLeaderboardId(leaderboard.getId());
                leaderboardResponse.setRoundId(round.getId());
                leaderboardResponse.setFinishType(round.getMatchType().getFinishType());
                leaderboardResponse.setTeamId(leaderboard.getTeam().getId());

                if (leaderboardResponse.getTopSpeed() == null && leaderboardResponse.getFastestLapTime() == null) {
                    Result result = getFastestLapTimeAndTopSpeed(round);

                    leaderboardResponse.setFastestLapTime(result.fastestLapTime());
                    leaderboardResponse.setTopSpeed(result.topSpeed());
                }

                leaderboardResponseList.add(leaderboardResponse);
            }
        }

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDir);

        // Sắp xếp danh sách theo Pageable
        List<LeaderboardResponse> sortedLeaderboards = leaderboardResponseList.stream()
                .sorted(getComparator(sortBy, sortDir))
                .collect(Collectors.toList());

        // Cắt danh sách theo Pageable
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedLeaderboards.size());
        List<LeaderboardResponse> pagedList = sortedLeaderboards.subList(start, end);

        // Tạo đối tượng Page từ danh sách đã phân trang
        Page<LeaderboardResponse> leaderboard = new PageImpl<>(pagedList, pageable, leaderboardResponseList.size());

        ListLeaderboardResponse response = new ListLeaderboardResponse();
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
                    fastestLapTime.setFastestLapTime(fastestLapTimeValue);
                    break;
                }
            }


            for (MatchTeam matchTeam : matchTeams) {

                // hết Test thì cmt đoạn này lại
                if (matchTeam.getScoreAttribute() == null) {
                    continue;
                }

                if (fastestLapTimeValue > matchTeam.getScoreAttribute().getFastestLapTime()) {
                    fastestLapTimeValue = matchTeam.getScoreAttribute().getFastestLapTime();
                    fastestLapTime.setId(matchTeam.getTeam().getId());
                    fastestLapTime.setTeamName(matchTeam.getTeam().getTeamName());
                    fastestLapTime.setTeamTag(matchTeam.getTeam().getTeamTag());
                    fastestLapTime.setAccountId(matchTeam.getAccount().getAccountId());
                    fastestLapTime.setFastestLapTime(fastestLapTimeValue);
                }

                if (topSpeedValue < matchTeam.getScoreAttribute().getTopSpeed()) {
                    topSpeedValue = matchTeam.getScoreAttribute().getTopSpeed();
                    topSpeed.setId(matchTeam.getTeam().getId());
                    topSpeed.setTeamName(matchTeam.getTeam().getTeamName());
                    topSpeed.setTeamTag(matchTeam.getTeam().getTeamTag());
                    topSpeed.setAccountId(matchTeam.getAccount().getAccountId());
                    topSpeed.setTopSpeed(topSpeedValue);
                }

            }
        }
        Result result = new Result(fastestLapTime, topSpeed);
        return result;
    }

    private record Result(FastestLapTimeTeam fastestLapTime, TopSpeedTeam topSpeed) {
    }

    private Comparator<LeaderboardResponse> getComparator(String sortBy, String sortDir) {

        Comparator<LeaderboardResponse> comparator;

        switch (sortBy) {
            case "roundId":
                comparator = Comparator.comparing(LeaderboardResponse::getRoundId);
                break;
            case "finishType":
                comparator = Comparator.comparing(LeaderboardResponse::getFinishType);
                break;
            case "fastestLapTime":
                comparator = Comparator.comparing(l -> l.getFastestLapTime() != null ? l.getFastestLapTime().getId() : Long.MAX_VALUE);
                break;
            case "topSpeed":
                comparator = Comparator.comparing(l -> l.getTopSpeed() != null ? l.getTopSpeed().getId() : Long.MAX_VALUE);
                break;
            case "teamId": // Đảm bảo lấy teamId của LeaderboardResponse, không lấy từ TeamTournamentResponse
                comparator = Comparator.comparing(LeaderboardResponse::getTeamId);
                break;
            default:
                comparator = Comparator.comparing(LeaderboardResponse::getLeaderboardId);
                break;
        }

        return sortDir.equalsIgnoreCase("desc") ? comparator.reversed() : comparator;
    }


}
