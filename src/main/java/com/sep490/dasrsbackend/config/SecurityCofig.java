package com.sep490.dasrsbackend.config;

import com.sep490.dasrsbackend.security.JwtAuthenticationEntryPoint;
import com.sep490.dasrsbackend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                        request.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/v1/auth/**", "/api/v1/leaderboards/**").permitAll()
                                .requestMatchers("/api/v1/environments/**", "/api/v1/match-types/**", "/api/v1/scored-methods/**", "/api/v1/resources/**").permitAll()
                                .requestMatchers("/api/v1/tournaments/**", "/api/v1/accounts/**").permitAll()
                                .requestMatchers("/api/v1/rounds/**", "/api/v1/cars/**").permitAll()
                                .requestMatchers("/api/v1/teams/**", "/api/v1/matches/**", "/api/v1/records/**", "/api/v1/complaints/**").permitAll()
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
