package com.project.arbaz.aaspass.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
@RequiredArgsConstructor
public class AaspassEmailService {
    // Method which will take the list of emails and then asynchronously sends the mails to the workers
    private final ExecutorService emailExecutorService;
    private final JavaMailSender mailSender;

    @Value("${GMAIL_USERNAME}")
    private String fromEmail;

    public void sendEmails(List<String> emails, String subject, String body) {

        for(String email : emails) {
            emailExecutorService.submit(() -> {
                try{
                    sendEmail(email, subject, body);
                }
                catch(Exception e){
                    log.error("Failed to send email to {}", email, e);
                }
            });
        }
    }

    private void sendEmail(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

}
