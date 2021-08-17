package com.dekut.dekutchat.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GetTime {
    String date;
    String time;
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");

    public GetTime() {

    }

    public String getDate(long timestamp) {
        cal.setTimeInMillis(timestamp);
        date = dateFormat.format(cal.getTime());
        return date;
    }

    public String getTime(long timestamp) {
        cal.setTimeInMillis(timestamp);
        time = timeFormat.format(cal.getTime());
        return time;
    }

    public boolean sameDay(long timestamp1, long timestamp2){
        boolean status = false;
        try {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTimeInMillis(timestamp1);
            cal2.setTimeInMillis(timestamp2);

            status = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }
}
