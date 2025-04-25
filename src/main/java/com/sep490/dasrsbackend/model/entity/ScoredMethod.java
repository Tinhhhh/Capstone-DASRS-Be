package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sep490.dasrsbackend.model.enums.FinishType;
import com.sep490.dasrsbackend.model.enums.MatchTypeStatus;
import com.sep490.dasrsbackend.model.enums.ScoredMethodStatus;
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
@Table(name = "scored_method")
@EntityListeners(AuditingEntityListener.class)
public class ScoredMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scored_method_id")
    private Long id;

    @Column(name = "lap", nullable = false)
    private double lap;

    @Column(name = "collision", nullable = false)
    private double collision;

    @Column(name = "off_track", nullable = false)
    private double offTrack;

    @Column(name = "total_race_time", nullable = false)
    private double totalRaceTime;

    @Column(name = "assist_usage", nullable = false)
    private double assistUsageCount;

    @Column(name = "average_speed", nullable = false)
    private double averageSpeed;

    @Column(name = "total_distance", nullable = false)
    private double totalDistance;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_finish_type", nullable = false)
    private FinishType matchFinishType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScoredMethodStatus status;

    @LastModifiedDate
    @Column(name = "last_modified_date",  nullable = false)
    private Date lastModifiedDate;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "scoredMethod")
    private List<Round> roundList;
}
