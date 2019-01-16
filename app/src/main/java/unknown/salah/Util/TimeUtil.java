package unknown.salah.Util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {
    public int getTimeUser(long milliToTime) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        java.util.TimeZone tzs = cal.getTimeZone();
        df.setTimeZone(TimeZone.getTimeZone(tzs.getID()));
        String current_time = df.format(milliToTime);

        return Integer.parseInt(current_time);
    }

    public long getInMillis(String userDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date = null;
        try {
            date = sdf.parse(userDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(new Date(date.getTime()));
        }
        return cal.getTimeInMillis();
    }

    public String getCurrentDate(){
        DateFormat dayt = new SimpleDateFormat("yyyyMMdd");
        return dayt.format(Calendar.getInstance().getTime());
    }

    public String convertDateFormat(String date){
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        try {
            Date mydate = df.parse(date);
            DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
            date = dateFormat.format(mydate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertDated(String date){
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        try {
            Date mydate = df.parse(date);
            DateFormat dateFormat = new SimpleDateFormat("dd MMMM");
            date = dateFormat.format(mydate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertToDay(String date){
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        try {
            Date mydate = df.parse(date);
            DateFormat dateFormat = new SimpleDateFormat("dd MMMM");
            date = dateFormat.format(mydate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertDateMonthFormat(String date){
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        try {
            Date mydate = df.parse(date);
            DateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
            date = dateFormat.format(mydate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
