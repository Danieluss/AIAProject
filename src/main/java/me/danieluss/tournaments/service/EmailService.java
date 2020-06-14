package me.danieluss.tournaments.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final EmailProperties properties;
    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender, EmailProperties properties) {
        this.javaMailSender = javaMailSender;
        this.properties = properties;
    }

    public void send(SimpleMailMessage email) {
        javaMailSender.send(email);
    }

    public void send(String to, String subject, String text) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        msg.setFrom(properties.getThisEmail());
        javaMailSender.send(msg);
    }

}
