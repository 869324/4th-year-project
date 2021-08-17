package com.dekut.dekutchat.utils;

public class TimeCalc {

    private static final long SECOND_MILLIS = 1000;
    private static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long MONTH_MILLIS = 30 * DAY_MILLIS;
    private static final long YEAR_MILLIS = 365 * DAY_MILLIS;

    public String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }


        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "now";
        }
        else if (diff < HOUR_MILLIS) {
            return diff/MINUTE_MILLIS +" min";
        }
        else if (diff < 2 * HOUR_MILLIS) {
            return "1 hour";
        }
        else if (diff < DAY_MILLIS) {
            return diff / HOUR_MILLIS + " hrs";
        }
        else if (diff < 2 * DAY_MILLIS) {
            return "1 day";
        }
        else if (diff < MONTH_MILLIS) {
            return diff / DAY_MILLIS + " days";
        }
        else if (diff < 2 * MONTH_MILLIS) {
            return "1 month";
        }
        else if (diff < YEAR_MILLIS) {
            return diff / MONTH_MILLIS + " months";
        }
        else if (diff < 2 * YEAR_MILLIS) {
            return "1 year";
        } else {
            return diff / YEAR_MILLIS + " years";
        }

    }
}
