package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
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
@Table(name = "tournament")
@EntityListeners(AuditingEntityListener.class)
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tournament_id")
    private Long id;

    @Column(name = "tournament_name", nullable = false)
    private String tournamentName;

    @Column(name = "context", nullable = false)
    private String context;

    @Column(name = "team_number", nullable = false)
    private int teamNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TournamentStatus status;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "tournament")
    private List<Round> roundList;

    @OneToMany(mappedBy = "tournament")
    private List<TournamentTeam> tournamentTeamList;

}
