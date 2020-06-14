package me.danieluss.tournaments.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.token")
public class TokenProperties {

    private Integer passwordExpiration;
    private Integer accountExpiration;

}
