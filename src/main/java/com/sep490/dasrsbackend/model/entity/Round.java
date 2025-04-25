package com.sep490.dasrsbackend.model.entity;

import com.sep490.dasrsbackend.model.enums.FinishType;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "round")
@EntityListeners(AuditingEntityListener.class)
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id")
    private Long id;

    @Column(name = "round_name", nullable = false)
    private String roundName;

    @Column(name = "round_duration",    nullable = false)
    private int roundDuration;

    @Column(name = "lap_number", nullable = false)
    private int lapNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "finish_type", nullable = false)
    private FinishType finishType;

    @Column(name = "team_limit", nullable = false)
    private int teamLimit;

    @Column(name = "is_last", nullable = false)
    private boolean isLast;

    @Column(name = "is_latest", nullable = false)
    private boolean isLatest;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoundStatus status;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "round")
    private List<Match> matchList;

    @OneToMany(mappedBy = "round")
    private List<Leaderboard> leaderboardList;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "scored_method_id", nullable = false)
    private ScoredMethod scoredMethod;

    @ManyToOne
    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    @ManyToOne
    @JoinColumn(name = "match_type_id", nullable = false)
    private MatchType matchType;

    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Resource resource;

}
