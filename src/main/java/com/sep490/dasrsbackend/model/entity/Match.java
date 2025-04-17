package com.sep490.dasrsbackend.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "match_name")
    private String matchName;

    @Column(name = "match_code")
    private String matchCode;

    @Column(name = "time_start")
    private Date timeStart;

    @Column(name = "time_end")
    private Date timeEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MatchStatus status;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "match")
    private List<MatchTeam> matchTeamList;

    @OneToMany(mappedBy = "match")
    private List<Record> recordList;

    @OneToMany(mappedBy = "match")
    private List<Complaint> complaintList;

    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

}
