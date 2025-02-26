package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.Payload.request.AuthenticationRequest;
import com.sep490.dasrsbackend.model.Payload.response.AuthenticationResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.security.NoSuchAlgorithmException;

public interface AuthenService {
//    void register(RegistrationRequest request) throws MessagingException;

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void logout(HttpServletRequest request);

    AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response);

    void forgotPassword(String email) throws NoSuchAlgorithmException, MessagingException;

    void resetPassword(String email, String token);

}
