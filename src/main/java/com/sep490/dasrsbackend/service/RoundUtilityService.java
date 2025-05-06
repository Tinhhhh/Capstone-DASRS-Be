package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.Util.DateUtil;
import com.sep490.dasrsbackend.Util.GenerateCode;
import com.sep490.dasrsbackend.Util.Schedule;
import com.sep490.dasrsbackend.model.entity.*;
import com.sep490.dasrsbackend.model.enums.MatchForm;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
import com.sep490.dasrsbackend.model.enums.MatchTeamStatus;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoundUtilityService {

    private final MatchRepository matchRepository;
    private final RoundRepository roundRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final TournamentTeamRepository tournamentTeamRepository;

    public boolean isMatchStarted(Long tournamentId) {
        List<Round> roundList = roundRepository.findAvailableRoundByTournamentId(tournamentId).stream().filter(round -> round.getStatus() == RoundStatus.ACTIVE).toList();

        if (roundList.isEmpty()) {
            return false;
        }

        for (Round round : roundList) {
            //Kiểm tra xem có match nào đã khởi động không
            //Kiểm tra getTimeStart before new Date() => match đã khởi động
            List<Match> mathList = matchRepository.findByRoundId(round.getId()).stream()
                    .filter(match -> match.getTimeStart().before(new Date())).toList();
            if (!mathList.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isMatchStartedForRound(Long roundId) {
        Date localDate = DateUtil.convertUtcToIctDate(Instant.now());
        List<Match> mathList = matchRepository.findByRoundId(roundId).stream()
                .filter(match -> match.getTimeStart().before(localDate)).toList();
        if (!mathList.isEmpty()) {
            return true;
        }
        return false;
    }

    public void terminateMatchesToRegenerate(Long roundId) {
        List<Match> matches = matchRepository.findByRoundId(roundId);
        for (Match match : matches) {
            List<MatchTeam> matchTeams = matchTeamRepository.findByMatchId(match.getId());
            matchTeamRepository.deleteAll(matchTeams);
        }
        matchRepository.deleteAll(matches);
    }


    public void generateMatch(Round round, Tournament tournament) {
        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournament.getId()).stream()
                .sorted(Comparator.comparing(Round::getTeamLimit).reversed()).toList();

        boolean isFirstRound = false;

        if (rounds.get(0).getId() == round.getId()) {
            isFirstRound = true;
        }

        MatchType matchType = round.getMatchType();
        double matchDuration = matchType.getMatchDuration() * 60;

        int numberOfPlayers = matchType.getPlayerNumber(); // số người mỗi team
        int numberOfTeams = matchType.getTeamNumber(); // số team

        //Số trận cần tạo
        double numberOfMatches;
        //Nếu là round đầu tiên thì số lượng match = số lượng team của tournament / số team trong 1 trận
        if (isFirstRound) {
            numberOfMatches = (double) tournament.getTeamNumber() / numberOfTeams;
        } else {
            //Nếu là round thứ 2 trở đi thì số lượng match = teamLimit của round trước / số team trong 1 trận
            numberOfMatches = (double) rounds.get(rounds.size() - 2).getTeamLimit() / numberOfTeams;
        }

        if (numberOfMatches % 1 != 0) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "The number of team is invalid for this match type, please check the number of team in tournament");
        }

        LocalDate localStartDate = DateUtil.convertToLocalDateTime(round.getStartDate()).toLocalDate();
        LocalDate localEndDate = DateUtil.convertToLocalDateTime(round.getEndDate()).toLocalDate();

        long daysBetween = ChronoUnit.DAYS.between(localStartDate, localEndDate);
        long result = daysBetween + 1;

        if (result < Math.ceil(numberOfMatches / Schedule.MAX_WORKING_HOURS)) {
            throw new DasrsException(HttpStatus.BAD_REQUEST,
                    "Request fails, the first round schedule need: " + (Math.ceil(numberOfMatches / Schedule.MAX_WORKING_HOURS) - result) + " days more to create matches");
        }


        LocalDateTime startTime = DateUtil.convertToLocalDateTime(round.getStartDate()).withHour(Schedule.WORKING_HOURS_START);
        LocalDateTime endTime = DateUtil.convertToLocalDateTime(round.getEndDate()).withHour(Schedule.WORKING_HOURS_END);

        LocalTime workStart = LocalTime.of(Schedule.WORKING_HOURS_START, 0);
        LocalTime workEnd = LocalTime.of(Schedule.WORKING_HOURS_END, 0);

        //Build string matchName
        StringBuilder stringBuilder = new StringBuilder();
        String seasonPrefix = GenerateCode.seasonPrefix(DateUtil.convertToLocalDateTime(tournament.getStartDate()));
        String matchTypePrefix = matchType.getMatchTypeCode();

        //Tạo từng match
        while (startTime.isBefore(endTime) && numberOfMatches > 0) {
            LocalTime currentTime = startTime.toLocalTime();
            // Nếu trong khung giờ làm việc (8:00 - 17:00), tăng biến đếm
            if (!currentTime.isBefore(workStart) && currentTime.isBefore(workEnd) && currentTime.getHour() != Schedule.LUNCH_BREAK_START) {

                stringBuilder.setLength(0);
                stringBuilder.append(matchType.getMatchTypeName());

                String name = stringBuilder.toString();

                //generate match code
                String matchCode;
                Optional<Match> isDuplicate;
                do {
                    //Ghép lại thành match code
                    stringBuilder.setLength(0);
                    matchCode = stringBuilder
                            .append(seasonPrefix)
                            .append("_")
                            .append(matchTypePrefix)
                            .append("_")
                            .append(GenerateCode.generateMatchCode())
                            .toString();

                    isDuplicate = matchRepository.findByMatchCode(matchCode);
                } while (isDuplicate.isPresent());

                Match match = Match.builder()
                        .matchName(name)
                        .matchCode(matchCode)
                        .timeStart(DateUtil.convertToDate(startTime))
                        .timeEnd(DateUtil.convertToDate(startTime.plusMinutes((long) matchDuration)))
                        .status(MatchStatus.PENDING)
                        .matchForm(MatchForm.OFFICIAL)
                        .round(round)
                        .build();

                matchRepository.save(match);

                startTime = startTime.plusHours(Schedule.SLOT_DURATION);
                numberOfMatches--;
            } else {
                if (currentTime.getHour() == Schedule.WORKING_HOURS_END) {
                    startTime = startTime.plusDays(1).withHour(Schedule.WORKING_HOURS_START);
                } else {
                    startTime = startTime.plusHours(Schedule.SLOT_DURATION);
                }
            }
        }

        generateMatchTeam(round, numberOfPlayers, numberOfTeams);

    }

    public void generateMatchTeam(Round round, int playersPerTeam, int teamsPerMatch) {

        int totalMatchTeam = playersPerTeam * teamsPerMatch;

        List<Match> matches = matchRepository.findByRoundId(round.getId()).stream()
                .filter(match -> match.getStatus() == MatchStatus.PENDING).toList();

        //Tạo sẵn matchTeam cho từng match
        for (Match match : matches) {
            for (int i = 0; i < totalMatchTeam; i++) {
                MatchTeam matchTeam = new MatchTeam();
                matchTeam.setMatch(match);
                matchTeam.setStatus(MatchTeamStatus.UNASSIGNED);
                matchTeam.setAttempt(0);
                matchTeamRepository.save(matchTeam);
            }
        }

        injectTeamToMatchTeam(round.getTournament().getId());
    }

    public void injectTeamToMatchTeam(Long tournamentId) {

        List<Round> rounds = roundRepository.findAvailableRoundByTournamentId(tournamentId).stream()
                .sorted(Comparator.comparing(Round::getTeamLimit).reversed()).toList();

        //trường hợp không có round nào đang hoạt động => không cần inject
        if (rounds.stream().filter(round -> round.getStatus() == RoundStatus.ACTIVE).count() == 0) {
            return;
        }

        //Xác định inject cho round nào
        //Mặc định láy round đầu tiên
        Round round = rounds.get(0);
        List<Leaderboard> leaderboards = new ArrayList<>();

        for (int i = 0; i < rounds.size(); i++) {
            if (rounds.get(i).getStatus() == RoundStatus.COMPLETED) {
                round = rounds.get(i + 1);
            }

            //Nếu round đang được inject không phải là round đầu tiên
            if (i >= 1) {
                if (rounds.get(i - 1).getStatus() == RoundStatus.COMPLETED) {
                    //Nếu round trước đó đã hoàn thành thì lấy leaderboard của round trước đó
                    leaderboards = leaderboardRepository.findByRoundId(rounds.get(i - 1).getId()).stream()
                            .sorted(Comparator.comparing(Leaderboard::getRanking))
                            .limit(rounds.get(i - 1).getTeamLimit())
                            .toList();
                }
            }

        }


        //Nếu round đầu tiên thì lấy team trong tournament
        List<Team> teams = tournamentTeamRepository.findByTournamentId(tournamentId).stream()
                .map(TournamentTeam::getTeam)
                .distinct().toList();

        if (!leaderboards.isEmpty()) {
            //Lấy danh sách team của round trước đó
            teams = leaderboards.stream()
                    .map(Leaderboard::getTeam)
                    .toList();
        }

        if (teams.isEmpty()) {
            return;
        }

        //Lấy danh sách matchTeam của round đang được inject
        List<Match> matches = matchRepository.findByRoundId(round.getId()).stream().filter(match -> match.getStatus() == MatchStatus.PENDING).toList();
        List<MatchTeam> matchTeams = new ArrayList<>();
        for (Match match : matches) {
            matchTeams.addAll(matchTeamRepository.findByMatchId(match.getId()));
        }

        //Lấy danh sách matchTeam chưa được gán team
        List<MatchTeam> unassignedMatchTeams = matchTeams.stream()
                .filter(matchTeam -> matchTeam.getTeam() == null)
                .sorted(Comparator.comparingLong(mt -> mt.getMatch().getId()))
                .toList();

        if (unassignedMatchTeams.isEmpty()) {
            return;
        }

        List<Team> assignedTeam = new ArrayList<>();

        //Lấy danh sách team đã được gán vào matchTeam
        for (Team t : teams) {
            List<Team> team = matchTeams.stream()
                    .map(MatchTeam -> {
                        if (MatchTeam.getTeam() == null) {
                            return null;
                        }

                        return MatchTeam.getTeam().getId() == t.getId() ? t : null;
                    })
                    .distinct().toList();

            if (!team.isEmpty()) {
                assignedTeam.addAll(team);
            }
        }

        //Lấy danh sách team chưa được gán vào matchTeam
        List<Team> unassignedTeams = new ArrayList<>(teams.stream()
                .filter(team -> !assignedTeam.contains(team))
                .toList());

        //Gán team vào matchTeam
        int numberOfTeamInMatchTeam = round.getMatchType().getPlayerNumber();

        Iterator<Team> iterator = unassignedTeams.iterator();
        int i = 0;
        int index = 0;
        outer:
        while (iterator.hasNext()) {
            Team team = iterator.next();

            if (team == null) {
                break;
            }

            do {

                if (index == numberOfTeamInMatchTeam) {
                    iterator.remove();
                    index = 0;
                    break;
                }

                unassignedMatchTeams.get(i).setTeam(team);
                index++;
                i++;
            } while (i < unassignedMatchTeams.size());
        }
        matchTeamRepository.saveAll(unassignedMatchTeams);

        //Gán tên team vào match
        unassignedMatchTeams.stream()
                .filter(mt -> mt.getTeam() != null)
                .collect(Collectors.groupingBy(MatchTeam::getMatch, Collectors.mapping(MatchTeam::getTeam, Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Team::getId))),
                        ArrayList::new
                ))))
                .entrySet().stream()
                .map(entry -> {
                    Match match = entry.getKey();
                    List<Team> teamList = entry.getValue();

                    StringBuilder matchName = new StringBuilder(match.getMatchName()).append(" - ");
                    for (int j = 0; j < teamList.size(); j++) {
                        matchName.append(teamList.get(j).getTeamName());
                        if (j < teamList.size() - 1) matchName.append(", ");
                    }
                    match.setMatchName(matchName.toString());
                    return match;
                }).toList();

        matchTeamRepository.saveAll(unassignedMatchTeams);
    }

    public void generateRematch(List<MatchTeam> matchTeams) {
        List<Round> rounds = matchTeams.stream()
                .map(m -> m.getMatch().getRound())
                .distinct()
                .toList();

        if (rounds.isEmpty()) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal error. Round not found");
        }

        if (rounds.size() > 1) {
            throw new DasrsException(HttpStatus.BAD_REQUEST, "Internal error. Multiple rounds found");
        }

        Round round = rounds.get(0);

        MatchType matchType = round.getMatchType();
        double matchDuration = matchType.getMatchDuration() * 60;

        //Số trận cần tạo
        double numberOfMatches = matchTeams.size();

        List<Match> matches = matchRepository.findByRoundId(round.getId()).stream()
                .sorted(Comparator.comparing(Match::getTimeEnd).reversed())
                .toList();

        Match lastMatch = matches.get(0);
        LocalDateTime matchEndTime = DateUtil.convertToLocalDateTime(lastMatch.getTimeEnd());
        LocalDateTime startTime = null;
        LocalDate today = LocalDate.now();

        //Trường hợp chưa tạo rematch nào => Rematch sẽ đc tạo vào ngày hôm sau của trận OFFICIAL cuối cùng
        if (lastMatch.getMatchForm() == MatchForm.OFFICIAL) {
            startTime = LocalDateTime.now().plusDays(1).withHour(Schedule.WORKING_HOURS_START).withMinute(0).withSecond(0).withNano(0);

            LocalDate endDate = DateUtil.convertToLocalDateTime(round.getEndDate()).toLocalDate();

            if (!endDate.isAfter(today)) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, " +
                        "Rematch cannot be created because the round has already ended, and rematches can only start from tomorrow.");
            }

            if (startTime.isAfter(DateUtil.convertToLocalDateTime(round.getEndDate()))) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, " +
                        "this round schedule need: " + (Math.ceil(numberOfMatches / Schedule.MAX_WORKING_HOURS)) + " days more to create matches");
            }
        }

        LocalTime workStart = LocalTime.of(Schedule.WORKING_HOURS_START, 0);
        LocalTime workEnd = LocalTime.of(Schedule.WORKING_HOURS_END, 0);

        //Trường hợp đã tạo rematch => Rematch sẽ đc tạo vào giờ tiếp theo của trận rematch cuối cùng
        if (lastMatch.getMatchForm() == MatchForm.REMATCH) {

            //Sort lại, laấy danh sách rematch theo thời gian bắt đầu sớm nhất => match 0 sẽ là match bắt đầu sớm nhất
            List<Match> rematchs = matches.stream()
                    .filter(match -> match.getMatchForm() == MatchForm.REMATCH)
                    .sorted(Comparator.comparing(Match::getTimeStart))
                    .toList();

            LocalDate rematch = DateUtil.convertToLocalDateTime(rematchs.get(0).getTimeStart()).toLocalDate();
//            LocalDate today = LocalDate.of(2025, 4, 21);

            //Nếu rematch bất kì đã bắt đầu thì không thể tạo rematch
            if (!rematch.isAfter(today)) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, " +
                        "Rematch creation deadline has passed as a rematch has already started or is scheduled for today: " +
                        DateUtil.formatTimestamp(rematchs.get(0).getTimeStart(), DateUtil.DATE_TIME_FORMAT));
            }

            startTime = matchEndTime.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            //Thời gian kết thúc của round
            LocalDateTime endRoundTime = DateUtil.convertToLocalDateTime(round.getEndDate()).withHour(Schedule.WORKING_HOURS_END);

            //Kiểm tra xem sau khi tạo rematch cuối cuùng có vượt quá thời gian kết thúc của round không

            LocalDateTime lastMatchEndTime = startTime;
            //Đánh dấu số trận có thể tạo trước khi qua ngày mới
            int index = 0;
            //Tạo flag đánh dấu đã qua ngày mới hay chưa
            boolean pass = false;
            for (int i = 0; i < numberOfMatches; i++) {
                LocalTime lastMatchEndTimeLocal = lastMatchEndTime.toLocalTime();

                //Nếu trong khung giờ làm việc (8:00 - 17:00), tăng biến đếm
                if (lastMatchEndTimeLocal.isAfter(workStart) && lastMatchEndTimeLocal.isBefore(workEnd)
                        && lastMatchEndTimeLocal.getHour() != Schedule.LUNCH_BREAK_START) {
                    lastMatchEndTime = lastMatchEndTime.plusHours(Schedule.SLOT_DURATION);
                    //Nếu chưa qua ngày mới thì cộng giá trị index
                    if (!pass) {
                        index++;
                    }
                } else {
                    //Nếu không trong khung giờ làm việc (8:00 - 17:00)
                    //Có thể là giờ nghỉ trưa hoặc sau giờ làm việc
                    //Nếu là sau giờ làm việc thì cộng thêm 1 ngày và quay về giờ làm việc
                    if (lastMatchEndTimeLocal.getHour() == Schedule.WORKING_HOURS_END) {
                        lastMatchEndTime = lastMatchEndTime.plusDays(1).withHour(Schedule.WORKING_HOURS_START);
                        //Đánh dấu đã qua ngày mới
                        pass = true;
                    }
                    //Nếu là giờ nghỉ trưa thì cộng thêm 1 tiếng
                    if (lastMatchEndTimeLocal.getHour() == Schedule.LUNCH_BREAK_START) {
                        lastMatchEndTime = lastMatchEndTime.plusHours(1);
                    }
                }

            }
            //Số ngày cần thêm = (số trận cần tạo - số trận có thể tạo) / 8 => ra số ngày.
            double days = (Math.ceil((numberOfMatches - index) / Schedule.MAX_WORKING_HOURS));
            if (lastMatchEndTime.isAfter(endRoundTime)) {
                throw new DasrsException(HttpStatus.BAD_REQUEST, "Request fails, " +
                        "this round schedule need: " + days + " days more to create matches");
            }

        }

        //Build string matchName
        StringBuilder stringBuilder = new StringBuilder();
        String seasonPrefix = GenerateCode.seasonPrefix(DateUtil.convertToLocalDateTime(round.getTournament().getStartDate()));
        String matchTypePrefix = "REMATCH";

        List<Match> rematchMatches = new ArrayList<>();
        //Tạo match
        while (numberOfMatches > 0) {
            LocalTime currentTime = startTime.toLocalTime();
            // Nếu trong khung giờ làm việc (8:00 - 17:00), tăng biến đếm
            if (!currentTime.isBefore(workStart) && currentTime.isBefore(workEnd) && currentTime.getHour() != Schedule.LUNCH_BREAK_START) {

                //generate match code
                String matchCode;
                Optional<Match> isDuplicate;
                do {
                    //Ghép lại thành match code
                    stringBuilder.setLength(0);
                    matchCode = stringBuilder
                            .append(seasonPrefix)
                            .append("_")
                            .append(matchTypePrefix)
                            .append("_")
                            .append(GenerateCode.generateMatchCode())
                            .toString();

                    isDuplicate = matchRepository.findByMatchCode(matchCode);
                } while (isDuplicate.isPresent());

                Match match = Match.builder()
                        .matchName("Rematch: Single player 1 Team")
                        .matchCode(matchCode)
                        .timeStart(DateUtil.convertToDate(startTime))
                        .timeEnd(DateUtil.convertToDate(startTime.plusMinutes((long) matchDuration)))
                        .status(MatchStatus.PENDING)
                        .matchForm(MatchForm.REMATCH)
                        .round(round)
                        .build();

                rematchMatches.add(matchRepository.save(match));

                startTime = startTime.plusHours(Schedule.SLOT_DURATION);
                numberOfMatches--;
            } else {
                if (currentTime.getHour() == Schedule.WORKING_HOURS_END) {
                    startTime = startTime.plusDays(1).withHour(Schedule.WORKING_HOURS_START);
                } else {
                    startTime = startTime.plusHours(Schedule.SLOT_DURATION);
                }
            }
        }

        //Tạo matchTeam cho từng match
        for (int i = 0; i < matchTeams.size(); i++) {
            rematchMatches.get(i).setMatchName(
                    rematchMatches.get(i).getMatchName() + " - " +
                            matchTeams.get(i).getTeam().getTeamName()
            );
            matchRepository.save(rematchMatches.get(i));

            MatchTeam matchTeam = new MatchTeam();
            matchTeam.setMatch(rematchMatches.get(i));
            matchTeam.setTeam(matchTeams.get(i).getTeam());
            matchTeam.setAccount(matchTeams.get(i).getAccount());
            matchTeam.setStatus(MatchTeamStatus.ASSIGNED);
            matchTeam.setScore(0);
            matchTeam.setAttempt(0);
            matchTeamRepository.save(matchTeam);
        }


    }
}
