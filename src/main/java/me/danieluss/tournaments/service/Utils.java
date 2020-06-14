package me.danieluss.tournaments.service;

import java.util.Date;

public class Utils {

    public static long hoursDiff(Date date0, Date date1) {
        return (date1.getTime() - date0.getTime()) / 1000 / 60 / 60;
    }
}
