package com.sep490.dasrsbackend.model.entity;

import com.sep490.dasrsbackend.model.enums.MapStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "map")
public class RaceMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Long id;

    @Column(name = "map_name")
    private String mapName;

    @Column(name = "map_image")
    private String mapImage;

    @Column(name = "status")
    private MapStatus status;

    @Column(name = "source")
    private String source;

    @OneToMany(mappedBy = "map")
    private List<Round> rounds;

}
