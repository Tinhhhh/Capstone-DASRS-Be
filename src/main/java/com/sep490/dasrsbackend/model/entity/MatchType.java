package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.FinishType;
import com.sep490.dasrsbackend.model.enums.MatchTypeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "match_type")
public class MatchType {
    @Id
    @GeneratedValue
    @Column(name = "match_type_id")
    @JsonProperty("match_type_id")
    private Long matchTypeId;

    @JsonProperty("match_type_name")
    @Column(name = "match_type_name")
    private String matchTypeName;

    @JsonProperty("finish_type")
    @Column(name = "finish_type")
    private FinishType finishType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MatchTypeStatus status;

    @OneToMany(mappedBy = "matchType")
    private List<Round> roundList;

}
