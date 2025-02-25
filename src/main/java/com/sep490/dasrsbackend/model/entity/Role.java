package com.exe201.ilink.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "role")
@EntityListeners(AuditingEntityListener.class)
public class Role {

    @Id
    @GeneratedValue
    @Column(name = "role_id")
    @JsonProperty("role_id")
    private Long roleId;

    @JsonProperty("role_name")
    @Column(name = "role_name")
    private String roleName;

    @JsonProperty("created_date")
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @JsonProperty("last_modified_date")
    @Column(name = "last_modified_date", insertable = false)
    private Date lastModifiedDate;


}
