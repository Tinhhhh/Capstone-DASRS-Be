package com.sep490.dasrsbackend.config;

import com.sep490.dasrsbackend.security.JwtAuthenticationEntryPoint;
import com.sep490.dasrsbackend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityCofig {

    private final UserDetailsService userDetailsService;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final JwtAuthenticationFilter authenticationFilter;

    private final LogoutHandler logoutHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .authorizeHttpRequests(request ->
                        //Authenticate
                        request.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/v1/auth/**").permitAll()
                                //Account
                                .requestMatchers(
                                        "/api/v1/accounts/landing/player-template",
                                        "/api/v1/accounts/landing/organizer-contacts"
                                ).permitAll()
                                .requestMatchers("/api/v1/accounts/change-password",
                                        "/api/v1/accounts/update-info").hasAnyAuthority("PLAYER", "ORGANIZER", "ADMIN")
                                .requestMatchers(
                                        "/api/v1/accounts/update-profile-picture").hasAnyAuthority("PLAYER", "ORGANIZER")
                                .requestMatchers(
                                        "/api/v1/accounts/staff-create",
                                        "/api/v1/accounts/import",
                                        "/api/v1/accounts/players").hasAuthority("ORGANIZER")
                                .requestMatchers(
                                        "/api/v1/accounts/lock",
                                        "/api/v1/accounts/edit",
                                        "/api/v1/accounts/by-admin",
                                        "/api/v1/accounts",
                                        "/api/v1/accounts/admin").hasAuthority("ADMIN")
                                //Match Type
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/match-types/*",
                                        "/api/v1/match-types").hasAnyAuthority("ORGANIZER", "ADMIN")
                                .requestMatchers("/api/v1/match-types/**").hasAuthority("ADMIN")
                                //Tournament
                                .requestMatchers(HttpMethod.GET, "/api/v1/tournaments").permitAll()
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/tournaments/*",
                                        "/api/v1/tournaments/teams/*").hasAnyAuthority("PLAYER", "ORGANIZER", "ADMIN")
                                .requestMatchers(
                                        "/api/v1/tournaments/register-team/**",
                                        "/api/v1/tournaments/team/*").hasAuthority("PLAYER")
                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/v1/tournaments/*",
                                        "/api/v1/tournaments/terminate/*",
                                        "/api/v1/tournaments/extend/*").hasAnyAuthority("ORGANIZER", "ADMIN")
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/v1/tournaments").hasAnyAuthority("ORGANIZER", "ADMIN")
                                .requestMatchers(
                                        "/api/v1/tournaments/dashboard/**").hasAnyAuthority("ORGANIZER", "ADMIN")
                                //Leaderboard
                                .requestMatchers(
                                        "/api/v1/leaderboards/**").permitAll()
                                //Complaint
                                .requestMatchers(
                                        "/api/v1/complaints/create").hasAuthority("PLAYER")
                                .requestMatchers(
                                        "/api/v1/complaints/update/*",
                                        "/api/v1/complaints/reply/*").hasAuthority("ORGANIZER")
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/complaints/*").hasAnyAuthority("PLAYER", "ORGANIZER")
                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/v1/complaints/*").hasAnyAuthority("PLAYER", "ORGANIZER")
                                .requestMatchers(
                                        "/api/v1/complaints/team/*",
                                        "/api/v1/complaints/round/*",
                                        "/api/v1/complaints/all",
                                        "/api/v1/complaints/match/*",
                                        "/api/v1/complaints/round/{roundId}/status/{status}").hasAnyAuthority("PLAYER", "ORGANIZER")
                                //resource
                                .requestMatchers("/api/v1/resources/map").hasAnyAuthority("PLAYER", "ORGANIZER")
                                .requestMatchers("/api/v1/resources/map/round/{roundId}").hasAnyAuthority("PLAYER", "ORGANIZER", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/resources/admin").hasAnyAuthority("ORGANIZER", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/resources/{resourceId}").permitAll()
                                .requestMatchers(HttpMethod.PUT, "/api/v1/resources/{resourceId}", "/api/v1/resources/change-status/{resourceId}").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/v1/resources").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/resources").permitAll()
                                //Match
                                .requestMatchers("/api/v1/matches/available", "/api/v1/matches/score-details/{matchId}/{teamId}").hasAnyAuthority("ORGANIZER", "PLAYER")
                                .requestMatchers("/api/v1/matches/round/{roundId}").hasAnyAuthority("ORGANIZER", "PLAYER")
                                .requestMatchers("/api/v1/matches/assign/{matchId}",
                                        "/api/v1/matches/tournament/{tournamentId}",
                                        "/api/v1/matches/team/{teamId}",
                                        "/api/v1/matches/round/{roundId}/team/{teamId}",
                                        "/api/v1/matches/by-round-and-player").hasAuthority("PLAYER")
                                .requestMatchers("/api/v1/matches/slot/{matchId}",
                                        "/api/v1/matches/round/{roundId}").hasAnyAuthority("ORGANIZER", "ADMIN")
                                .requestMatchers("/api/v1/matches/rematch",
                                        "/api/v1/matches/score-details/{matchId}/{teamId}").hasAuthority("ORGANIZER")
                                .requestMatchers("/api/v1/matches/score-data",
                                        "/api/v1/matches/car-data",
                                        "/api/v1/matches/unity").hasAuthority("PLAYER")
                                //Team
                                .requestMatchers(HttpMethod.GET, "/api/v1/teams/**").hasAnyAuthority("ORGANIZER", "PLAYER", "ADMIN")
                                .requestMatchers("/api/v1/teams/**").hasAuthority("PLAYER")
                                //Car
                                .requestMatchers(HttpMethod.GET, "/api/v1/cars/**").permitAll()
                                .requestMatchers("/api/v1/cars/**").hasAuthority("ADMIN")
                                //Round
                                .requestMatchers(HttpMethod.GET, "/api/v1/rounds").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/rounds/{roundId}").permitAll()
                                .requestMatchers("/api/v1/rounds/landing").permitAll()
                                .requestMatchers(HttpMethod.GET,
                                        "/api/v1/rounds/tournament/{tournamentId}",
                                        "/api/v1/rounds/team/{teamId}/tournament/{tournamentId}").hasAnyAuthority("ORGANIZER", "PLAYER", "ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/rounds",
                                        "/api/v1/rounds/terminate/{roundId}").hasAnyAuthority("ORGANIZER", "ADMIN")
                                .requestMatchers("/api/v1/rounds/extend/{roundId}").hasAuthority("ORGANIZER")
                                .requestMatchers("/api/v1/rounds/account/{accountId}").hasAuthority("PLAYER")
                                //Environment
                                .requestMatchers(HttpMethod.GET, "/api/v1/environments/**").permitAll()
                                .requestMatchers("/api/v1/environments/**").hasAuthority("ADMIN")
                                //Scored Method
                                .requestMatchers(HttpMethod.GET, "/api/v1/scored-methods/{scoredMethodId}").hasAnyAuthority("ORGANIZER", "PLAYER", "ADMIN")
                                .requestMatchers("/api/v1/scored-methods/**").hasAnyAuthority("ORGANIZER", "PLAYER", "ADMIN")
                                .anyRequest().authenticated()
                )
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        http.logout(logout -> logout.logoutUrl("api/v1/account/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication)
                        -> SecurityContextHolder.clearContext()));

        return http.build();
    }


}
