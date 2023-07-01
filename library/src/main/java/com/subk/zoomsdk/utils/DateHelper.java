package com.subk.zoomsdk.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DateHelper {
    public static Calendar getDateInstance(int daysFromToday) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, daysFromToday);
        return calendar;
    }

    public static Calendar getDateInstance(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        return calendar;
    }

    public static String getFormattedDateTime(int daysFromToday, String format) {
        Calendar calendar = getDateInstance(daysFromToday);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        return dateFormat.format(calendar.getTime());
    }

    public static String getFormattedDateTime(Calendar calendar, int daysFromCalendar, String format) {
        calendar.add(Calendar.DATE, daysFromCalendar);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        return dateFormat.format(calendar.getTime());
    }

    public static String getFormattedDateTime(int year, int month, int dayOfMonth, String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        return dateFormat.format(calendar.getTime());
    }

    public static String formatStringDateTime(String dateTime, String inputFormat, String outputFormat) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(inputFormat, Locale.ENGLISH);
        Date parsedDate = Objects.requireNonNull(dateFormat.parse(dateTime));
        dateFormat = new SimpleDateFormat(outputFormat, Locale.ENGLISH);
        return dateFormat.format(parsedDate);// all done
    }

    public static String formatServerDateTime(String dateTime) {
        try {
            return formatStringDateTime(dateTime, "yyyy-MM-dd'T'HH:mm:ss.SSS", "dd MMM, yyyy hh:mm a");
        } catch (ParseException e) {
            Log.e(DateHelper.class.getName(), "Error while parsing server date time : " + e.getMessage());
        }
        return "";
    }
}
