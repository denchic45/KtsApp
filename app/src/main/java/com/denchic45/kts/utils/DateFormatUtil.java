package com.denchic45.kts.utils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class DateFormatUtil {

    private static final Calendar calToday = Calendar.getInstance();
    public static String yyy_MM_dd = "yyyy-MM-dd";
    public static String LLLL = "LLLL";
    public static String LLLL_yyyy = "LLLL yyyy";
    public static String DD_MM_yy = "dd.MM.yy";
    public static final String dd_MMM = "dd MMM";
    public static final String dd_MMMM = "dd MMMM";

    public static Date convertStringToDate(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date convertedDate = null;
        try {
            convertedDate = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    public static Date convertStringToDateUTC(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date convertedDate = null;
        try {
            convertedDate = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    @NotNull
    public static String convertDateToString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    @NotNull
    public static String convertDateToStringUTC(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    @NotNull
    public static String convertDateToStringHidingCurrentYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (cal.get(Calendar.YEAR) == calToday.get(Calendar.YEAR)) {
            return StringUtils.capitalize(convertDateToString(date, LLLL));
        } else {
            return StringUtils.capitalize(convertDateToString(date, LLLL_yyyy));
        }
    }

    @NotNull
    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static boolean validateDateOfString(String date , String format) {
        return convertStringToDate(date, format) != null;
    }

    public static Date convertDateToDateUTC(Date date) {
        return convertStringToDateUTC(convertDateToString(date, yyy_MM_dd), yyy_MM_dd);
    }
}
