package me.danieluss.tournaments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TournamentsApp {

    public static void main(String[] args) {
        SpringApplication.run(TournamentsApp.class, args);
    }
}
