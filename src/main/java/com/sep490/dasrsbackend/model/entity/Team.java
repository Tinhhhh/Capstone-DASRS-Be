package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sep490.dasrsbackend.model.enums.TeamStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "team")
@EntityListeners(AuditingEntityListener.class)
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Column(name = "team_tag", unique = true,nullable = false)
    private String teamTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private TeamStatus status;

    @Column(name = "is_disqualified", nullable = false)
    private boolean isDisqualified;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "team")
    private List<TournamentTeam> tournamentTeamList;

    @OneToMany(mappedBy = "team")
    private List<Account> accountList;

    @OneToMany(mappedBy = "team")
    private List<Leaderboard> leaderboardList;

    @OneToMany(mappedBy = "team")
    private List<MatchTeam> matchTeams;

}
