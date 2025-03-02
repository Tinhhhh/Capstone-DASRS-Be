package com.sep490.dasrsbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.RecordStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "record")
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_link")
    private String recordLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_score")
    private RecordStatus status;

    @ManyToOne
    @JoinColumn(name="match_id", nullable = false)
    private Match match;
}
