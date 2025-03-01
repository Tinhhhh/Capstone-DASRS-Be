package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.model.entity.Role;
import com.sep490.dasrsbackend.model.enums.EmailTemplateName;
import com.sep490.dasrsbackend.model.exception.RegisterAccountExistedException;
import com.sep490.dasrsbackend.model.payload.request.AuthenticationRequest;
import com.sep490.dasrsbackend.model.payload.request.NewAccountByAdmin;
import com.sep490.dasrsbackend.model.payload.response.AuthenticationResponse;
import com.sep490.dasrsbackend.model.entity.AccessToken;
import com.sep490.dasrsbackend.model.entity.Account;
import com.sep490.dasrsbackend.model.entity.RefreshToken;
import com.sep490.dasrsbackend.model.exception.DasrsException;
import com.sep490.dasrsbackend.repository.AccessTokenRepository;
import com.sep490.dasrsbackend.repository.AccountRepository;
import com.sep490.dasrsbackend.repository.RefreshTokenRepository;
import com.sep490.dasrsbackend.repository.RoleRepository;
import com.sep490.dasrsbackend.security.JwtTokenProvider;
import com.sep490.dasrsbackend.service.AuthenService;
import com.sep490.dasrsbackend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenServiceImpl implements AuthenService {
    private final AccountRepository accountRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new DasrsException(HttpStatus.UNAUTHORIZED, "Authentication fails, your email is not exist"));

        //Create new token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        //Terminate all existing tokens
        revokeRefreshTokens(accessToken);
        revokeAccessTokensFromUser(account);

        //Save new token
        saveAccountToken(account, accessToken, refreshToken);

        return AuthenticationResponse
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void revokeRefreshTokens(String accessToken) {
        AccessToken token = accessTokenRepository.findByToken(accessToken);
        if (token != null) {
            //Terminate old refreshToken
            RefreshToken refreshToken = token.getRefreshToken();
            token.setRevoked(true);
            token.setExpired(true);
            accessTokenRepository.save(token);
        }

    }

    private void saveAccountToken(Account account, String accessToken, String refreshToken) {
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .revoked(false)
                .expired(false)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        AccessToken newAccessToken = AccessToken.builder()
                .token(accessToken)
                .account(account)
                .refreshToken(newRefreshToken)
                .revoked(false)
                .expired(false)
                .build();

        accessTokenRepository.save(newAccessToken);

    }

    private void revokeAccessTokensFromUser(Account account) {
        List<AccessToken> existingToken = accessTokenRepository.findAllValidTokensByUser(account.getAccountId());
        if (!existingToken.isEmpty()) {
            existingToken.forEach(token -> {
                token.setRevoked(true);
                token.setExpired(true);
                accessTokenRepository.save(token);
            });
        }

    }

    @Override
    public void logout(HttpServletRequest request) {

    }

    @Override
    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public void forgotPassword(String email) throws NoSuchAlgorithmException, MessagingException {

    }

    @Override
    public void resetPassword(String email, String token) {

    }

    @Override
    public void newAccountByAdmin(NewAccountByAdmin request) throws MessagingException {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new DasrsException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration fails, role not found !"));

        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegisterAccountExistedException("Account already exists");
        }

        Account account = Account.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .address(request.getAddress())
                .phone(request.getPhone())
                .gender("")
                .dob(null)
                .password(passwordEncoder.encode(request.getPassword()))
                .isLocked(false)
                .isLeader(false)
                .role(role)
                .team(null)
                .build();
        accountRepository.save(account);
        sendRegistrationEmail(account);
    }

    private void sendRegistrationEmail(Account account) throws MessagingException {
        emailService.sendMimeMessageWithHtml(
                account.fullName(), account.getEmail(), account.getPassword(), account.getEmail(),
                EmailTemplateName.ADMIN_CREATE_ACCOUNT.getName(), "[Dasrs] Thông tin tài khoản của bạn");
    }
}
