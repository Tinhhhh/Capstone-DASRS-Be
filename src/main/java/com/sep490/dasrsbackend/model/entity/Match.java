package com.sep490.dasrsbackend.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sep490.dasrsbackend.model.enums.MatchForm;
import com.sep490.dasrsbackend.model.enums.MatchStatus;
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
@Table(name = "match")
@EntityListeners(AuditingEntityListener.class)
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long id;

    @Column(name = "match_name", nullable = false)
    private String matchName;

    @Column(name = "match_code", nullable = false)
    private String matchCode;

    @Column(name = "time_start", nullable = false)
    private Date timeStart;

    @Column(name = "time_end", nullable = false)
    private Date timeEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MatchStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_form", nullable = false)
    private MatchForm matchForm;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "match")
    private List<MatchTeam> matchTeamList;

    @OneToMany(mappedBy = "match")
    private List<Record> recordList;

    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

}
