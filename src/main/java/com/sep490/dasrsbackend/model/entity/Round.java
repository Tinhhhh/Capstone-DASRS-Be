package com.sep490.dasrsbackend.model.entity;

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

    @Column(name = "round_name")
    private String roundName;

    @Column(name = "team_limit")
    private int teamLimit;

    @Column(name = "is_last")
    private boolean isLast;

    @Column(name = "is_latest")
    private boolean isLatest;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoundStatus status;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
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
    @JoinColumn(name = "resource_id", nullable = true)
    private Resource resource;

}
