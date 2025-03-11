package com.sep490.dasrsbackend.service;

import com.sep490.dasrsbackend.model.payload.request.AuthenticationRequest;
import com.sep490.dasrsbackend.model.payload.request.NewAccountByAdmin;
import com.sep490.dasrsbackend.model.payload.response.AuthenticationResponse;
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

    void resetPassword(String password, String token);


}
