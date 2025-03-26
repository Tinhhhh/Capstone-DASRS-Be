package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.model.entity.Leaderboard;
import com.sep490.dasrsbackend.model.entity.Round;
import com.sep490.dasrsbackend.model.entity.Team;
import com.sep490.dasrsbackend.model.payload.response.LeaderboardResponse;
import com.sep490.dasrsbackend.model.payload.response.ListLeaderboardResponse;
import com.sep490.dasrsbackend.repository.LeaderboardRepository;
import com.sep490.dasrsbackend.repository.RoundRepository;
import com.sep490.dasrsbackend.repository.TeamRepository;
import com.sep490.dasrsbackend.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
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

    @Override
    public void updateLeaderboard(List<Leaderboard> leaderboards) {
        leaderboards.sort(Comparator.comparing(Leaderboard::getTeamScore).reversed());

        int rank = 1;
        for (Leaderboard leaderboard : leaderboards) {
            leaderboard.setRanking(rank++);
        }

        leaderboardRepository.saveAll(leaderboards);
    }

    @Override
    public ListLeaderboardResponse getLeaderboardByRoundId(Long roundId, int pageNo, int pageSize, String sortBy, String sortDir) {

        Round round = roundRepository.findById(roundId).orElseThrow(
                () -> new IllegalArgumentException("Request fails, round not found")
        );

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Leaderboard> leaderboards = leaderboardRepository.findByRoundId(round.getId(), pageable);

        return getListLeaderboardResponse(leaderboards);
    }

    @Override
    public ListLeaderboardResponse getLeaderboardByTeamId(Long teamId, int pageNo, int pageSize, String sortBy, String sortDir) {

        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new IllegalArgumentException("Request fails, team not found")
        );

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Leaderboard> leaderboards = leaderboardRepository.findByTeamId(team.getId(), pageable);

        return getListLeaderboardResponse(leaderboards);
    }

    private static Pageable getPageable(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(pageNo, pageSize, sort);
    }

    private ListLeaderboardResponse getListLeaderboardResponse(Page<Leaderboard> leaderboards) {
        List<LeaderboardResponse> leaderboardList = leaderboards.stream()
                .map(leaderboard -> {
                    LeaderboardResponse lbr = modelMapper.map(leaderboard, LeaderboardResponse.class);
                    lbr.setRoundId(leaderboard.getRound().getId());
                    lbr.setTeamId(leaderboard.getTeam().getId());
                    lbr.setCreatedDate(DateUtil.formatTimestamp(leaderboard.getCreatedDate()));
                    return lbr;
                })
                .toList();

        ListLeaderboardResponse response = new ListLeaderboardResponse();
        response.setContent(leaderboardList);
        response.setTotalPages(leaderboards.getTotalPages());
        response.setTotalElements(leaderboards.getTotalElements());
        response.setPageNo(leaderboards.getNumber());
        response.setPageSize(leaderboards.getSize());
        response.setLast(leaderboards.isLast());

        return response;
    }

    @Override
    public ListLeaderboardResponse getLeaderboardByTournamentId(Long tournamentId, int pageNo, int pageSize, String sortBy, String sortDir) {

        List<Round> rounds = roundRepository.findByTournamentId(tournamentId);

        List<Leaderboard> leaderboards = new ArrayList<>();

        for (Round round : rounds) {
            List<Leaderboard> leaderboardPage = leaderboardRepository.findByRoundIdNotDisqualified(round.getId());
            leaderboards.addAll(leaderboardPage);
        }

        Pageable pageable = getPageable(pageNo, pageSize, sortBy, sortDir);

        // Sắp xếp danh sách theo Pageable
        List<Leaderboard> sortedLeaderboards = leaderboards.stream()
                .sorted(getComparator(sortBy, sortDir))
                .collect(Collectors.toList());

        // Cắt danh sách theo Pageable
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedLeaderboards.size());
        List<Leaderboard> pagedList = sortedLeaderboards.subList(start, end);

        // Tạo đối tượng Page từ danh sách đã phân trang
        Page<Leaderboard> leaderboard = new PageImpl<>(pagedList, pageable, leaderboards.size());

        return getListLeaderboardResponse(leaderboard);
    }

    private Comparator<Leaderboard> getComparator(String sortBy, String sortDir) {

        Comparator<Leaderboard> comparator = Comparator.comparing(Leaderboard::getRanking);
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

}
