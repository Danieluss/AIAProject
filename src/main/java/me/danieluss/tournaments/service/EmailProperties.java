package me.danieluss.tournaments.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.validation.constraints.Email;
import java.util.Properties;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.mail")
public class EmailProperties {

    @Email
    private String thisEmail;
    private String password;

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setUsername(thisEmail);
        javaMailSender.setPassword(password);

        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.ssl.enable", "true");
        properties.setProperty("mail.debug", "true");
        properties.setProperty("mail.smtp.host", "smtp.poczta.onet.pl");
        properties.setProperty("mail.smtp.port", "465");
        properties.setProperty("mail.smtp.ssl.trust", "*");
        javaMailSender.setJavaMailProperties(properties);
        return javaMailSender;
    }
}
