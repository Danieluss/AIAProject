package me.danieluss.tournaments.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class URLService {

    private Environment environment;

    @Autowired
    public URLService(Environment environment) {
        this.environment = environment;
    }

//    mock some domain
    public String getThisURL() {
        String host = "localhost";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return String.format("https://%s:%s/", host, environment.getProperty("local.server.port"));

    }
}
