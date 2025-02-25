package com.exe201.ilink.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "account", indexes = {
    @Index(name = "idx_email", columnList = "email"),
})
public class Account implements UserDetails, Principal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty("account_id")
    @Column(name = "account_id")
    private UUID accountId;

    @JsonProperty("first_name")
    @Column(name = "first_name")
    private String firstName;

    @JsonProperty("last_name")
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "gender")
    private String gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "phone")
    private String phone;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "is_locked")
    private boolean isLocked;

    @Column(name = "is_enable")
    private boolean isEnable;

    @JsonIgnore
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @JsonIgnore
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;


    @JsonIgnore
    @Override
    public String getName() {
        return email;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return email;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return isLocked;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return isEnable;
    }

    @JsonIgnore
    public String fullName() {
        return firstName + " " + lastName;
    }


}
