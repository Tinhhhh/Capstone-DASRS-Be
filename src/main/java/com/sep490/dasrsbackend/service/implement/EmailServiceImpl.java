package com.sep490.dasrsbackend.service.implement;

import com.sep490.dasrsbackend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    public static final String UTF_8_CODING = "UTF-8";
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    @Async
    public void sendMimeMessageWithHtml(String name, String email, String password, String to, String template, String subject) throws MessagingException {
        try {
            String senderNickName = "Customer Service Team at Dasrs";
            Context context = new Context();
            context.setVariable("username", name);
            context.setVariable("email", email);
            context.setVariable("password", password);
            extractTemplate(to, template, subject, senderNickName, context);
        } catch (Exception exception) {
            System.out.println("Error: " + exception.getMessage());
            throw new RuntimeException("Error: " + exception.getMessage());
        }
    }

    private void extractTemplate(String sentTo, String template, String subject, String senderNickName, Context context) throws MessagingException, UnsupportedEncodingException {
        String text = templateEngine.process(template, context);
        MimeMessage message = getMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_CODING);
        helper.setSubject(subject);
        helper.setPriority(1);
        helper.setFrom(sender, senderNickName);
        helper.setTo(sentTo);
        helper.setText(text, true);
        mailSender.send(message);
    }

    public MimeMessage getMessage() {
        return mailSender.createMimeMessage();
    }
}
