package com.sep490.dasrsbackend.security;

import com.sep490.dasrsbackend.model.entity.AccessToken;
import com.sep490.dasrsbackend.repository.AccessTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetail;
    private final AccessTokenRepository accessTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //get jwt token from header
        String jwt = getJwtFromRequest(request);

        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            //get username from jwt token
            String username = jwtTokenProvider.getUsernameFromJwt(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetail.loadUserByUsername(username);

                AccessToken token = accessTokenRepository.findByToken(jwt);

                if (jwtTokenProvider.isTokenValid(jwt, userDetails.getUsername())
                        && token != null
                        && !token.isRevoked()
                        && !token.isExpired()
                ) {
                    UsernamePasswordAuthenticationToken authenToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authenToken.setDetails(new WebAuthenticationDetailsSource()
                            .buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
