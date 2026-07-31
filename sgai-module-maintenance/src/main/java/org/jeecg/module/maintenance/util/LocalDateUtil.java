package org.jeecg.module.maintenance.util;


import org.jeecg.module.maintenance.exception.TimeException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 描述:
 *
 * @author ppliu created in 2019/11/5 10:09
 */
public class LocalDateUtil {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    /** yyyy-MM-dd */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    /** yyyy-MM-dd HH:mm:ss */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /** HH:mm:ss */
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取一段时间内的所有日期.
     *
     * @param start
     *            开始日期.
     * @param end
     *            结束日期.
     * @return 所有的日期集合.
     */
    public static List<LocalDate> getAllDaysWithRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new TimeException("开始时间不能晚于结束时间");
        }
        return Stream.iterate(start, localDate -> localDate.plusDays(1)).limit(ChronoUnit.DAYS.between(start, end) + 1)
                .collect(Collectors.toList());
    }

    /**
     * 获取一段时间内的所有日期.
     *
     * @param start
     *            开始日期.
     * @param end
     *            结束日期.
     * @param pattern
     *            转换格式.
     * @return 所有的日期集合.
     */
    public static List<LocalDate> getAllDaysWithRange(String start, String end, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate startDate = LocalDate.parse(start, dateTimeFormatter);
        LocalDate endDate = LocalDate.parse(end, dateTimeFormatter);
        return getAllDaysWithRange(startDate, endDate);
    }

    /**
     * 判断该日期是否在日期时间段内.
     *
     * @param start
     *            开始日期.
     * @param end
     *            结束日期.
     * @param localDate
     *            需要判断的日期.
     * @return 判定结果.
     */
    public static boolean isBetween(LocalDate start, LocalDate end, LocalDate localDate) {
        return localDate.isAfter(start.plusDays(-1)) && localDate.isBefore(end.plusDays(1));
    }

    /**
     * @param date
     *            需要转换的事件
     * @return localDate
     */
    public static LocalDate dateToLocalDate(Date date) {
        String strDate = SIMPLE_DATE_FORMAT.format(date);
        return LocalDate.parse(strDate, DATE_TIME_FORMATTER);
    }

    /**
     * @param localDate
     *            需要转换的时间.
     * @return date
     */
    public static Date localDateToDate(LocalDate localDate) throws ParseException {
        String strDate = localDate.format(DATE_TIME_FORMATTER);
        return SIMPLE_DATE_FORMAT.parse(strDate);
    }

    /**
     * 获得当前时间的yyyy-MM-dd格式字符串
     *
     * @return String
     */
    public static String getCurrentDate() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDate today = LocalDate.now();
        String nowDate = today.format(df);
        return nowDate;
    }

    /**
     * LocalDate转化为指定格式字符串
     *
     * @param fromDate
     * @param dateFormat
     * @return
     */
    public static String convertLocalDateToString(LocalDate fromDate, String dateFormat) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(dateFormat);
        if (fromDate != null) {
            String dateStr = fromDate.format(df);
            return dateStr;
        }
        return null;
    }

    /**
     * LocalDateTime转化为指定格式字符串
     *
     * @param fromDateTime
     * @param dateTimeFotmat
     * @return
     */
    public static String convertLocalDateTimeToString(LocalDateTime fromDateTime, String dateTimeFotmat) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(dateTimeFotmat);
        if (fromDateTime != null) {
            String localTime = fromDateTime.format(df);
            return localTime;
        }
        return null;
    }

    /**
     * 时间格式字符串转化为指定格式的时间
     *
     * @param beginDate
     * @param dateFormat
     * @return
     */
    public static LocalDate convertStringToLocalDate(String beginDate, String dateFormat) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(dateFormat);
        try {
            LocalDate fromDate = LocalDate.parse(beginDate, df);
            return fromDate;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 时间格式字符串转化为指定格式的时间
     *
     * @param beginDateTime
     * @param dateTimeFormat
     * @return
     */
    public static LocalDateTime convertStringToLocalDateTime(String beginDateTime, String dateTimeFormat) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(dateTimeFormat);
        try {
            LocalDateTime fromDateTime = LocalDateTime.parse(beginDateTime, df);
            return fromDateTime;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Date转换为LocalDate
     *
     * @param date
     * @return
     */
    public static LocalDate convertDateToLocalDate(Date date) {
        if (null == date) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Date转换为LocalDateTime
     *
     * @param dateTime
     * @return
     */
    public static LocalDateTime convertDateToLocalDateTime(Date dateTime) {
        if (null == dateTime) {
            return null;
        }
        return LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime转换为Date
     *
     * @param time
     * @return
     */
    public static Date convertLDTToDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获得毫秒数
     *
     * @param localDateTime
     * @return
     */
    public static long getTimestampOfDateTime(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * 获取本月第一天
     *
     * @return
     */
    public static LocalDate getFirstDayOfCurrentMonth() {
        LocalDate currentDay = LocalDate.now();
        return currentDay.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 获取本月最后一天
     *
     * @return
     */
    public static LocalDate getLastDayOfCurrentMonth() {
        LocalDate currentDay = LocalDate.now();
        return currentDay.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 获取本月最后一天
     *
     * @return
     */
    public static LocalDate getLastDayOfMonth(LocalDate month) {
        return month.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 获取当天开始时间 2020-05-29 00:00:00
     *
     * @return
     */
    public static LocalDateTime getTodayBeginTime() {
        LocalDate currentDay = LocalDate.now();
        return LocalDateTime.of(currentDay, LocalTime.MIN);
    }

    /**
     * 获取当天结束时间 2020-05-29 23:59:59
     *
     * @return
     */
    public static LocalDateTime getTodayEndTime() {
        LocalDate currentDay = LocalDate.now();
        return LocalDateTime.of(currentDay, LocalTime.MAX);
    }

    /**
     * 获取本周开始时间 2020-05-29 00:00:00
     *
     * @return
     */
    public static LocalDateTime getWeekBeginTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        int currentOrdinal = currentDateTime.getDayOfWeek().ordinal();
        return currentDateTime.minusDays(currentOrdinal).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * 获取本周开始时间 2020-05-29 00:00:00
     *
     * @return
     */
    public static String getWeekBeginTimeString() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        int currentOrdinal = currentDateTime.getDayOfWeek().ordinal();
        LocalDateTime weekBeginDateTime = currentDateTime.minusDays(currentOrdinal).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        return convertLocalDateTimeToString(weekBeginDateTime, DATETIME_FORMAT);
    }

    /**
     * 获取本周结束时间 2020-05-29 23:59:59
     *
     * @return
     */
    public static LocalDateTime getWeekEndTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        int currentOrdinal = currentDateTime.getDayOfWeek().ordinal();
        return currentDateTime.plusDays(6 - currentOrdinal).withHour(23).withMinute(59).withSecond(59)
                .withNano(999999999);
    }

    /**
     * 获取本周结束时间字符串 2020-05-29 23:59:59
     *
     * @return
     */
    public static String getWeekEndTimeString() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        int currentOrdinal = currentDateTime.getDayOfWeek().ordinal();
        LocalDateTime weekEndDateTime = currentDateTime.plusDays(6 - currentOrdinal).withHour(23).withMinute(59)
                .withSecond(59).withNano(999999999);
        return convertLocalDateTimeToString(weekEndDateTime, DATETIME_FORMAT);
    }

    public static List<Long> getTerm(LocalDate startDate, LocalDate endDate) {
        int start_year = startDate.getYear();
        int start_month = startDate.getMonthValue();
        int start_day = startDate.getDayOfMonth();
        int end_year = endDate.getYear();
        int end_month = endDate.getMonthValue();
        int end_day = endDate.getDayOfMonth();
        long y = ChronoUnit.YEARS.between(startDate, endDate); // 获取两个日期间隔年
        long m = ChronoUnit.MONTHS.between(startDate, endDate); // 获取两个日期间隔月
        long d = ChronoUnit.DAYS.between(startDate, endDate); // 获取两个日期间隔天

        int lastDayOfEndDate = endDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth(); // 获取某个月的最后一天
        if (start_year == end_year) {
            if (start_day == end_day || lastDayOfEndDate == end_day) {
                m = end_month - start_month;
            } else {
                d = endDate.getDayOfMonth(); // 获取传入时间对象的Day值
            }
        } else {
            if (m >= 12) {
                m = m - y * 12;
            }
            if (start_day == end_day || lastDayOfEndDate == end_day) {
                d = 0;
            } else {
                d = endDate.getDayOfMonth();
            }
        }
        List<Long> list = new ArrayList<>();
        list.add(y);
        list.add(m);
        list.add(d);
        return list;
    }
}
