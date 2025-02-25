package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.RoundStatus;
import com.sep490.dasrsbackend.model.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    @JsonProperty("round_id")
    @Column(name = "round_id")
    private Long roundId;

    @JsonProperty("round_name")
    @Column(name = "round_name")
    private String roundName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoundStatus status;

    @JsonProperty("start_date")
    @Column(name = "start_date")
    private Date startDate;

    @JsonProperty("end_date")
    @Column(name = "end_date")
    private Date endDate;

    @LastModifiedDate
    @JsonProperty("last_modified_date")
    @Column(name = "last_modified_date", insertable = false)
    private Date lastModifiedDate;

    @CreatedDate
    @JsonProperty("created_date")
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

}
