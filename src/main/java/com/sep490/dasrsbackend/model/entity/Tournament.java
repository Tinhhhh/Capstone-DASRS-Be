package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.TeamStatus;
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
    @GeneratedValue
    @Column(name = "tournament_id")
    @JsonProperty("tournament_id")
    private Long id;

    @JsonProperty("tournament_name")
    @Column(name = "tournament_name")
    private String tournamentName;

    @Column(name = "context")
    private String context;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TournamentStatus status;

    @JsonProperty("start_date")
    @Column(name = "start_date")
    private Date startDate;

    @JsonProperty("end_date")
    @Column(name = "end_date")
    private Date endDate;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "tournament")
    private List<Round> roundList;

}
