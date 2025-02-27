package com.sep490.dasrsbackend.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendMimeMessageWithHtml(String name,String email, String password, String to, String template, String subject) throws MessagingException;
}
