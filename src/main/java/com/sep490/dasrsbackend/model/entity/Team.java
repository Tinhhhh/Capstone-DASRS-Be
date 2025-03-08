package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.TeamStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "team")
@EntityListeners(AuditingEntityListener.class)
public class Team {
    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "team_tag")
    private String teamTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TeamStatus status;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @OneToMany(mappedBy = "team")
    private List<Account> accountList;

    @OneToMany(mappedBy = "team")
    private List<Leaderboard> leaderboardList;

    @OneToMany(mappedBy = "team")
    private List<MatchTeam> matchTeams;

}
