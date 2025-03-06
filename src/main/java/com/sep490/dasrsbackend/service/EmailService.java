package com.sep490.dasrsbackend.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendAccountInformation(String name,String email, String password, String to, String template, String subject) throws MessagingException;
    void sendMimeMessageWithHtml(String name, String to, String content, String template, String subject) throws MessagingException;
}
