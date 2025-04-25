package com.sep490.dasrsbackend.model.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep490.dasrsbackend.model.enums.EnvironmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "environment")
@EntityListeners(AuditingEntityListener.class)
public class Environment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "environment_id")
    private Long id;

    @Column(name = "environment_name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnvironmentStatus status;

    @OneToMany(mappedBy = "environment")
    private List<Round> roundList;

}
